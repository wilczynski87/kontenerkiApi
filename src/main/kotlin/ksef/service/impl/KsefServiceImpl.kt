package com.kontenery.ksef.service.impl

import com.kontenery.KsefConfig
import com.kontenery.ksef.KsefTokenDiagnostics
import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.crypto.KsefTokenEncryptor
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefInvoiceQueryDateRange
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.mapper.InvoiceToKsefFa3Mapper
import com.kontenery.ksef.repository.KsefRepository
import com.kontenery.ksef.service.KsefService
import com.kontenery.repository.KsefSessionInvoiceStatusRepo
import com.kontenery.service.InvoiceService
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class KsefServiceImpl(
    private val config: KsefConfig,
    private val repository: KsefRepository,
    private val invoiceService: InvoiceService,
    private val ksefSessionInvoiceStatusRepo: KsefSessionInvoiceStatusRepo,
) : KsefService {

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedAccessTokenValidUntil: Instant? = null

    override suspend fun login(): KsefLoginResponse {
        val tokens = authenticate()
        return KsefLoginResponse(
            authenticated = true,
            validUntil = tokens.second,
        )
    }

    override suspend fun listInvoicesKsef(
        from: String?,
        to: String?,
        pageOffset: Int,
        pageSize: Int,
        subjectType: String,
    ): KsefInvoiceListResponse {
        require(pageSize in 10..250) {
            "pageSize must be between 10 and 250"
        }

        val accessToken = obtainAccessToken()
        val dateRange = buildDateRange(from, to)
        val filters = KsefInvoiceQueryFilters(
            subjectType = subjectType,
            dateRange = dateRange,
        )

        val response = repository.queryInvoices(
            accessToken = accessToken,
            pageOffset = pageOffset,
            pageSize = pageSize,
            sortOrder = "Asc",
            filters = filters,
        )

        return KsefInvoiceListResponse(
            invoices = response.invoices,
            hasMore = response.hasMore ?: false,
            pageOffset = pageOffset,
            pageSize = pageSize,
        )
    }

    override suspend fun sendInvoiceToKsefByNumber(invoiceNumber: String): KsefSendInvoiceResponse {
        val invoice = invoiceService.getInvoiceByNumber(invoiceNumber)
            ?: throw KsefException("Invoice not found: $invoiceNumber", statusCode = 404)
        return sendInvoiceToKsef(invoice)
    }

    override suspend fun sendInvoiceToKsef(invoice: Invoice): KsefSendInvoiceResponse {
        val invoiceXml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice).toByteArray(StandardCharsets.UTF_8)
        val accessToken = obtainAccessToken()
        val encryptionData = repository.createEncryptionData()
        val session = repository.openOnlineSession(accessToken, encryptionData)

        try {
            val sendResponse = repository.sendInvoiceToSession(
                accessToken = accessToken,
                sessionReferenceNumber = session.referenceNumber,
                invoiceXml = invoiceXml,
                encryptionData = encryptionData,
            )

            val invoiceStatus = awaitInvoiceProcessed(
                accessToken = accessToken,
                sessionReferenceNumber = session.referenceNumber,
                invoiceReferenceNumber = sendResponse.referenceNumber,
            )

            val deferredSessionStatus = runCatching {
                ksefSessionInvoiceStatusRepo.save(invoice.invoiceNumber!!, invoiceStatus)
                null
            }.getOrElse { invoiceStatus }

            return KsefSendInvoiceResponse(
                sessionReferenceNumber = session.referenceNumber,
                invoiceReferenceNumber = sendResponse.referenceNumber,
                ksefNumber = invoiceStatus.ksefNumber,
                invoiceNumber = invoiceStatus.invoiceNumber ?: invoice.invoiceNumber,
                sessionStatus = deferredSessionStatus,
            )
        } catch (e: KsefException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw KsefException(
                message = e.message ?: "Invalid invoice for KSeF",
                statusCode = 400,
                cause = e,
            )
        } catch (e: Exception) {
            throw KsefException(
                message = "KSeF invoice send failed: ${e.message ?: e.javaClass.simpleName}",
                cause = e,
            )
        } finally {
            runCatching { repository.closeOnlineSession(accessToken, session.referenceNumber) }
        }
    }

    override suspend fun persistSessionStatus(
        invoiceNumber: String,
        status: KsefSessionInvoiceStatusResponse,
    ) {
        ksefSessionInvoiceStatusRepo.save(invoiceNumber, status)
    }

    private suspend fun awaitInvoiceProcessed(
        accessToken: String,
        sessionReferenceNumber: String,
        invoiceReferenceNumber: String,
    ): KsefSessionInvoiceStatusResponse {
        repeat(INVOICE_POLL_MAX_ATTEMPTS) { attempt ->
            val status = repository.fetchSessionInvoiceStatus(
                accessToken,
                sessionReferenceNumber,
                invoiceReferenceNumber,
            )
            if (!status.ksefNumber.isNullOrBlank() || status.permanentStorageDate != null) {
                return status
            }
            val code = status.status?.code
            if (code != null && code !in INVOICE_PENDING_CODES) {
                throw KsefException(
                    "KSeF invoice processing failed: $code ${status.status.description}",
                )
            }
            if (attempt < INVOICE_POLL_MAX_ATTEMPTS - 1) {
                delay(INVOICE_POLL_INTERVAL_MS)
            }
        }
        throw KsefException("KSeF invoice processing timed out")
    }

    private suspend fun obtainAccessToken(): String {
        val now = Clock.System.now()
        val cached = cachedAccessToken
        val validUntil = cachedAccessTokenValidUntil
        if (cached != null && validUntil != null && now < validUntil.minus(60, DateTimeUnit.SECOND)) {
            return cached
        }
        val (token, validUntilStr) = authenticate()
        cachedAccessToken = token
        cachedAccessTokenValidUntil = validUntilStr?.let { Instant.parse(it) }
        return token
    }

    private suspend fun authenticate(): Pair<String, String?> {
        val ksefToken = config.token?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw KsefException(
                "KSEF_TOKEN is not set (environment=${config.environment}, api=${config.baseUrl}). " +
                    "Generate a system token in the KSeF ${config.environment} portal for NIP ${config.nip ?: "?"} " +
                    "and add it to .env or KSEF_TOKEN_FILE.",
            )
        KsefTokenDiagnostics.validateForAuthentication(ksefToken, config.nip)
        val nip = config.nip?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw KsefException(
                "KSEF_NIP is not set (environment=${config.environment}). " +
                    "Use the NIP of the entity the KSEF_TOKEN was issued for.",
            )

        val tokenEncryptionCert = resolveTokenEncryptionCertificate(repository.fetchPublicKeyCertificates())
        val (challenge, timestampMs) = repository.fetchAuthChallenge()
        val encryptedToken = KsefTokenEncryptor.encryptToken(
            ksefToken = ksefToken,
            timestampMs = timestampMs,
            certificateBase64 = tokenEncryptionCert.certificate,
        )

        val authResponse = repository.submitKsefTokenAuth(
            challenge = challenge,
            nip = nip,
            encryptedToken = encryptedToken,
            publicKeyId = tokenEncryptionCert.publicKeyId,
        )

        awaitAuthReady(
            referenceNumber = authResponse.referenceNumber,
            authenticationToken = authResponse.authenticationToken.token,
        )

        val tokens = repository.redeemAuthToken(authResponse.authenticationToken.token)
        return tokens.accessToken.token to tokens.accessToken.validUntil
    }

    private suspend fun awaitAuthReady(referenceNumber: String, authenticationToken: String) {
        repeat(AUTH_POLL_MAX_ATTEMPTS) { attempt ->
            val status = repository.fetchAuthStatus(referenceNumber, authenticationToken)
            when (status.status.code) {
                AUTH_SUCCESS_CODE -> return
                in AUTH_PENDING_CODES -> {
                    if (attempt < AUTH_POLL_MAX_ATTEMPTS - 1) {
                        delay(AUTH_POLL_INTERVAL_MS)
                    }
                }
                else -> throw KsefException(
                    "KSeF authentication failed [${config.environment}, ${config.baseUrl}]: " +
                        "${status.status.code} ${status.status.description}. " +
                        "Token must be generated for the same environment and NIP (${config.nip}). " +
                        "In DEV use KSEF_ENV=TEST and api-test.ksef.mf.gov.pl.",
                )
            }
        }
        throw KsefException("KSeF authentication timed out after ${AUTH_POLL_MAX_ATTEMPTS} attempts")
    }

    private fun resolveTokenEncryptionCertificate(
        certificates: List<KsefPublicKeyCertificate>,
    ): KsefPublicKeyCertificate {
        val now = Clock.System.now()
        return certificates
            .filter { cert ->
                cert.usage.any { it.equals("KsefTokenEncryption", ignoreCase = true) }
            }
            .filter { cert ->
                val validFrom = cert.validFrom?.let { Instant.parse(it) }
                val validTo = cert.validTo?.let { Instant.parse(it) }
                (validFrom == null || validFrom <= now) && (validTo == null || now < validTo)
            }
            .maxByOrNull { it.validFrom?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST }
            ?: throw KsefException("No valid KSeF token encryption certificate found")
    }

    private fun buildDateRange(from: String?, to: String?): KsefInvoiceQueryDateRange {
        val now = Clock.System.now()
        val defaultTo = now.toString()
        val defaultFrom = now.minus(365, DateTimeUnit.DAY, TimeZone.UTC).toString()

        return KsefInvoiceQueryDateRange(
            dateType = "Invoicing",
            from = from ?: defaultFrom,
            to = to ?: defaultTo,
        )
    }

    companion object {
        private const val AUTH_SUCCESS_CODE = 200
        private val AUTH_PENDING_CODES = setOf(100, 150)
        private const val AUTH_POLL_MAX_ATTEMPTS = 30
        private const val AUTH_POLL_INTERVAL_MS = 2_000L
        private val INVOICE_PENDING_CODES = setOf(100, 150)
        private const val INVOICE_POLL_MAX_ATTEMPTS = 30
        private const val INVOICE_POLL_INTERVAL_MS = 2_000L
    }
}
