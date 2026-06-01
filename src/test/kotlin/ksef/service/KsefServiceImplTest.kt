package com.kontenery.ksef.service

import com.kontenery.KsefConfig
import com.kontenery.ksef.crypto.KsefEncryptionData
import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefEncryptionInfo
import com.kontenery.ksef.dto.KsefInvoiceMetadata
import com.kontenery.ksef.dto.KsefOpenOnlineSessionResponse
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.dto.KsefStatusInfo
import com.kontenery.ksef.dto.KsefTokenInfo
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.repository.KsefRepository
import com.kontenery.ksef.service.impl.KsefServiceImpl
import com.kontenery.repository.KsefSessionInvoiceStatusRepo
import com.kontenery.service.InvoiceService
import com.kontenery.testfixtures.sampleVatInvoice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KsefServiceImplTest {

    private val config = KsefConfig(
        environment = "TEST",
        baseUrl = "https://api-test.ksef.mf.gov.pl",
        apiSuffix = "v2",
        token = "test-token|nip-1234567890|abcdef0123456789abcdef0123456789abcdef0123456789ab",
        nip = "1234567890",
    )

    @Test
    fun `listInvoices returns metadata from repository`() = runBlocking {
        val repository = mockk<KsefRepository>()
        coEvery { repository.fetchPublicKeyCertificates() } returns listOf(
            KsefPublicKeyCertificate(
                certificate = TEST_CERTIFICATE_BASE64,
                publicKeyId = "key-1",
                usage = listOf("KsefTokenEncryption"),
            ),
        )
        coEvery { repository.fetchAuthChallenge() } returns Pair("challenge-123", 1_700_000_000_000L)
        coEvery { repository.submitKsefTokenAuth(any(), any(), any(), any()) } returns KsefSignatureResponse(
            referenceNumber = "ref-1",
            authenticationToken = KsefTokenInfo(token = "temp-token"),
        )
        coEvery { repository.fetchAuthStatus(any(), any()) } returns KsefAuthStatusResponse(
            status = KsefStatusInfo(code = 200, description = "OK"),
        )
        coEvery { repository.redeemAuthToken(any()) } returns KsefAuthOperationStatusResponse(
            accessToken = KsefTokenInfo(token = "access-token", validUntil = "2099-01-01T00:00:00Z"),
        )
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns KsefQueryInvoiceMetadataResponse(
            invoices = listOf(KsefInvoiceMetadata(ksefNumber = "KSeF-1", invoiceNumber = "FV/1")),
            hasMore = false,
        )

        val invoiceService = mockk<InvoiceService>()
        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo)
        val result = service.listInvoicesKsef(pageOffset = 0, pageSize = 10)

        assertEquals(1, result.invoices.size)
        assertEquals("KSeF-1", result.invoices.first().ksefNumber)
    }

    @Test
    fun `listInvoices rejects invalid page size`() {
        val repository = mockk<KsefRepository>()
        val invoiceService = mockk<InvoiceService>()
        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.listInvoicesKsef(pageSize = 5) }
        }
    }

    @Test
    fun `downloadInvoiceFromKsef returns xml by ksef number`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        coEvery { repository.downloadInvoiceByKsefNumber("access-token", "KSeF-99") } returns
            "<Faktura/>".toByteArray()
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns
            KsefQueryInvoiceMetadataResponse(
                invoices = listOf(KsefInvoiceMetadata(ksefNumber = "KSeF-99", invoiceNumber = "FV/9")),
                hasMore = false,
            )

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.downloadInvoiceFromKsef(ksefNumber = "KSeF-99")

        assertEquals("KSeF-99", result.ksefNumber)
        assertEquals("<Faktura/>", result.xml)
        assertEquals("FV/9", result.invoiceNumber)
    }

    @Test
    fun `downloadInvoiceFromKsef resolves ksef number by invoice number`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        val metadata = KsefInvoiceMetadata(ksefNumber = "KSeF-1", invoiceNumber = "FV/1")
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns
            KsefQueryInvoiceMetadataResponse(invoices = listOf(metadata), hasMore = false)
        coEvery { repository.downloadInvoiceByKsefNumber("access-token", "KSeF-1") } returns
            "<Faktura id=\"1\"/>".toByteArray()

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.downloadInvoiceFromKsef(invoiceNumber = "FV/1")

        assertEquals("KSeF-1", result.ksefNumber)
        assertTrue(result.xml.contains("Faktura"))
    }

    @Test
    fun `downloadInvoicesForMonthFromKsef downloads all with ksef numbers`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns
            KsefQueryInvoiceMetadataResponse(
                invoices = listOf(
                    KsefInvoiceMetadata(ksefNumber = "KSeF-A", invoiceNumber = "FV/A"),
                    KsefInvoiceMetadata(ksefNumber = null, invoiceNumber = "FV/B"),
                ),
                hasMore = false,
            )
        coEvery { repository.downloadInvoiceByKsefNumber("access-token", "KSeF-A") } returns
            "<A/>".toByteArray()

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.downloadInvoicesForMonthFromKsef(2025, 5)

        assertEquals(1, result.downloadedCount)
        assertEquals(1, result.skippedWithoutKsefNumber)
        assertEquals("KSeF-A", result.invoices.single().ksefNumber)
    }

    @Test
    fun `isInvoiceRegisteredInKsef returns true when ksef number present`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns
            KsefQueryInvoiceMetadataResponse(
                invoices = listOf(
                    KsefInvoiceMetadata(
                        ksefNumber = "KSeF-1",
                        invoiceNumber = "FV/1",
                        invoicingDate = "2025-05-15T10:00:00Z",
                        permanentStorageDate = "2025-05-15T10:01:00Z",
                    ),
                ),
                hasMore = false,
            )

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.isInvoiceRegisteredInKsef("FV/1")

        assertEquals(true, result.registered)
        assertEquals("KSeF-1", result.ksefNumber)
        assertEquals("FV/1", result.invoiceNumber)
    }

    @Test
    fun `isInvoiceRegisteredInKsef returns false when not in ksef`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        coEvery { repository.queryInvoices(any(), any(), any(), any(), any()) } returns
            KsefQueryInvoiceMetadataResponse(invoices = emptyList(), hasMore = false)

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.isInvoiceRegisteredInKsef("FV/404")

        assertEquals(false, result.registered)
        assertEquals(null, result.ksefNumber)
    }

    @Test
    fun `downloadInvoiceFromKsef requires identifier`() {
        val service = KsefServiceImpl(config, mockk(), mockk(), mockk(relaxed = true))
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.downloadInvoiceFromKsef() }
        }
    }

    @Test
    fun `sendInvoiceToKsefByNumber fails when invoice not found`() {
        val repository = mockk<KsefRepository>()
        val invoiceService = mockk<InvoiceService>()
        coEvery { invoiceService.getInvoiceByNumber("FV/99") } returns null

        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo)

        val ex = assertThrows(KsefException::class.java) {
            runBlocking { service.sendInvoiceToKsefByNumber("FV/99") }
        }
        assertEquals(404, ex.statusCode)
    }

    @Test
    fun `login returns authenticated with validUntil from KSeF`() = runBlocking {
        val repository = mockk<KsefRepository>()
        coEvery { repository.fetchPublicKeyCertificates() } returns listOf(
            KsefPublicKeyCertificate(
                certificate = TEST_CERTIFICATE_BASE64,
                publicKeyId = "key-1",
                usage = listOf("KsefTokenEncryption"),
            ),
        )
        coEvery { repository.fetchAuthChallenge() } returns Pair("challenge-123", 1_700_000_000_000L)
        coEvery { repository.submitKsefTokenAuth(any(), any(), any(), any()) } returns KsefSignatureResponse(
            referenceNumber = "ref-1",
            authenticationToken = KsefTokenInfo(token = "temp-token"),
        )
        coEvery { repository.fetchAuthStatus(any(), any()) } returns KsefAuthStatusResponse(
            status = KsefStatusInfo(code = 200, description = "OK"),
        )
        coEvery { repository.redeemAuthToken(any()) } returns KsefAuthOperationStatusResponse(
            accessToken = KsefTokenInfo(token = "access-token", validUntil = "2099-01-01T00:00:00Z"),
        )

        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))
        val result = service.login()

        assertEquals(true, result.authenticated)
        assertEquals("2099-01-01T00:00:00Z", result.validUntil)
    }

    @Test
    fun `sendInvoiceToKsef completes session and returns ksef number`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubSendInvoiceSession(repository, ksefNumber = "KSeF-123")
        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, mockk(), ksefSessionInvoiceStatusRepo)
        val invoice = sampleVatInvoice()

        val result = service.sendInvoiceToKsef(invoice)

        assertEquals("sess-ref-1", result.sessionReferenceNumber)
        assertEquals("inv-ref-1", result.invoiceReferenceNumber)
        assertEquals("KSeF-123", result.ksefNumber)
        assertEquals(invoice.invoiceNumber, result.invoiceNumber)
        coVerify { repository.closeOnlineSession("access-token", "sess-ref-1") }
        coVerify { ksefSessionInvoiceStatusRepo.save(invoice.invoiceNumber!!, any()) }
    }

    @Test
    fun `sendInvoiceToKsefByNumber loads invoice and sends`() = runBlocking {
        val repository = mockk<KsefRepository>()
        stubSendInvoiceSession(repository)
        val invoice = sampleVatInvoice()
        val invoiceService = mockk<InvoiceService>()
        coEvery { invoiceService.getInvoiceByNumber("FV/1/2025") } returns invoice
        val service = KsefServiceImpl(config, repository, invoiceService, mockk(relaxed = true))

        val result = service.sendInvoiceToKsefByNumber("FV/1/2025")

        assertEquals("KSeF-123", result.ksefNumber)
    }

    @Test
    fun `sendInvoiceToKsef fails when KSeF returns processing error`() {
        val repository = mockk<KsefRepository>()
        stubAuthenticatedRepository(repository)
        stubSendInvoiceSession(repository, processingStatusCode = 450, processingDescription = "Invalid FA")
        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))

        val ex = assertThrows(KsefException::class.java) {
            runBlocking { service.sendInvoiceToKsef(sampleVatInvoice()) }
        }
        assertTrue(ex.message!!.contains("450"))
    }

    @Test
    fun `sendInvoiceToKsef rejects invoice without invoice number`() {
        val repository = mockk<KsefRepository>()
        val service = KsefServiceImpl(config, repository, mockk(), mockk(relaxed = true))

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.sendInvoiceToKsef(sampleVatInvoice().copy(invoiceNumber = null)) }
        }
    }

    @Test
    fun `sendInvoiceToKsef returns deferred session status when persistence fails`() = runBlocking {
        val repository = mockk<KsefRepository>()
        val sessionStatus = KsefSessionInvoiceStatusResponse(
            ksefNumber = "KSeF-456",
            invoiceNumber = "FV/1/2025",
            status = KsefStatusInfo(code = 200, description = "OK"),
        )
        stubSendInvoiceSession(repository, ksefNumber = "KSeF-456", sessionStatus = sessionStatus)
        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>()
        coEvery { ksefSessionInvoiceStatusRepo.save(any(), any()) } throws RuntimeException("db unavailable")
        val service = KsefServiceImpl(config, repository, mockk(), ksefSessionInvoiceStatusRepo)

        val result = service.sendInvoiceToKsef(sampleVatInvoice())

        assertEquals("KSeF-456", result.ksefNumber)
        assertEquals(sessionStatus, result.sessionStatus)
    }

    @Test
    fun `login fails when token is missing`() {
        val service = KsefServiceImpl(
            config.copy(token = null),
            mockk(),
            mockk(),
            mockk(relaxed = true),
        )

        assertThrows(KsefException::class.java) {
            runBlocking { service.login() }
        }
    }

    private fun stubSendInvoiceSession(
        repository: KsefRepository,
        ksefNumber: String = "KSeF-123",
        invoiceRef: String = "inv-ref-1",
        sessionRef: String = "sess-ref-1",
        processingStatusCode: Int? = null,
        processingDescription: String = "OK",
        sessionStatus: KsefSessionInvoiceStatusResponse? = null,
    ) {
        stubAuthenticatedRepository(repository)
        val encryptionData = KsefEncryptionData(
            cipherKey = ByteArray(32),
            cipherIv = ByteArray(16),
            encryptionInfo = KsefEncryptionInfo(
                encryptedSymmetricKey = "encrypted-key",
                initializationVector = "iv",
            ),
        )
        coEvery { repository.createEncryptionData() } returns encryptionData
        coEvery { repository.openOnlineSession(any(), any()) } returns KsefOpenOnlineSessionResponse(
            referenceNumber = sessionRef,
        )
        coEvery { repository.sendInvoiceToSession(any(), any(), any(), any()) } returns KsefSendInvoiceOnlineResponse(
            referenceNumber = invoiceRef,
        )
        val resolvedStatus = sessionStatus ?: KsefSessionInvoiceStatusResponse(
            referenceNumber = invoiceRef,
            invoiceNumber = "FV/1/2025",
            ksefNumber = if (processingStatusCode == null) ksefNumber else null,
            status = KsefStatusInfo(
                code = processingStatusCode ?: 200,
                description = processingDescription,
            ),
            permanentStorageDate = if (processingStatusCode == null) "2025-05-15T10:00:00Z" else null,
        )
        coEvery {
            repository.fetchSessionInvoiceStatus("access-token", sessionRef, invoiceRef)
        } returns resolvedStatus
        coEvery { repository.closeOnlineSession(any(), any()) } returns Unit
    }

    private fun stubAuthenticatedRepository(repository: KsefRepository) {
        coEvery { repository.fetchPublicKeyCertificates() } returns listOf(
            KsefPublicKeyCertificate(
                certificate = TEST_CERTIFICATE_BASE64,
                publicKeyId = "key-1",
                usage = listOf("KsefTokenEncryption"),
            ),
        )
        coEvery { repository.fetchAuthChallenge() } returns Pair("challenge-123", 1_700_000_000_000L)
        coEvery { repository.submitKsefTokenAuth(any(), any(), any(), any()) } returns KsefSignatureResponse(
            referenceNumber = "ref-1",
            authenticationToken = KsefTokenInfo(token = "temp-token"),
        )
        coEvery { repository.fetchAuthStatus(any(), any()) } returns KsefAuthStatusResponse(
            status = KsefStatusInfo(code = 200, description = "OK"),
        )
        coEvery { repository.redeemAuthToken(any()) } returns KsefAuthOperationStatusResponse(
            accessToken = KsefTokenInfo(token = "access-token", validUntil = "2099-01-01T00:00:00Z"),
        )
    }

    companion object {
        private const val TEST_CERTIFICATE_BASE64 =
            "MIIDBzCCAe+gAwIBAgIUGao5uR6TgnLKrRUWonXnf8eh6r0wDQYJKoZIhvcNAQELBQAwEzERMA8GA1UEAwwIS1NlRlRlc3QwHhcNMjYwNTIxMDk0MDE0WhcNMjcwNTIxMDk0MDE0WjATMREwDwYDVQQDDAhLU2VGVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALe+KoOiOaUXH6JdQdu4YMLrnSkU4NHK8FMKO8vx009xO777YWgNLRx1vf+yneUBMgZWD20MC1/oBsi+EYpw237kv7wjmHUjA7UABH/SWJuoIRPFh2kgoI3mtvyVseG4RRojEVfc8p1NmUOkcnr31zFKNrYJ6V2iW+DOFYUaBaiDfri9ZNUWgfOLVQhX5gsGtTaVMleKskdQ5/ggjpMkWBQ/juYAgzAFkG7Kdf1AXVL27tiGr0er6R+oM+cgeJAxsDK4x0itFetxzKSnZROBkEO2gfSUymfmDZIJeNiQ4BhUQZWxJ14ivp8OjAKOnOgdpSsXzrkYDhT0rTMsvzSnnF0CAwEAAaNTMFEwHQYDVR0OBBYEFO6RnO86QivOF1R83CCaHEXyh3ehMB8GA1UdIwQYMBaAFO6RnO86QivOF1R83CCaHEXyh3ehMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAGQimdtmBoFM9DlxneSUXpWeWZjg5fk96VgqTH25taVz1dGh3zIKhIDzS4h3nKjhDGfPOHzH2nfCfr8q30cIZoxLBP1iQ30djRapTI3WXQ69dtKRWY7lHDpQz0Cb8xxws2y8WSN7RLur+73rkopBxPY+sWYf3MHNJXdOBX9Py29MiEXJ2K+s74EObxqC8pjpCVi6zY8Wy5krCAGK6OlHue8srlXMB70w/YFcbNZI3earvdj7k/xYSgTBrwG2uSqKXZ5AnD4dbYchi+Tr70K2LIgVXVg3aSUagbuMe2QWhP/bR7t/uPLdPrwjg9DnxbhHlNSEEF6xbap/cg45cAYf35U="
    }
}
