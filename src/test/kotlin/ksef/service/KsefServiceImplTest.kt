package com.kontenery.ksef.service

import com.kontenery.KsefConfig
import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefInvoiceMetadata
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.dto.KsefStatusInfo
import com.kontenery.ksef.dto.KsefTokenInfo
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.repository.KsefRepository
import com.kontenery.ksef.service.impl.KsefServiceImpl
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.KsefSessionInvoiceStatusRepo
import com.kontenery.service.InvoiceService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KsefServiceImplTest {

    private val config = KsefConfig(
        baseUrl = "https://api-test.ksef.mf.gov.pl",
        apiSuffix = "v2",
        token = "secret-token",
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
        val invoiceRepo = mockk<InvoiceRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo, invoiceRepo)
        val result = service.listInvoices(pageOffset = 0, pageSize = 10)

        assertEquals(1, result.invoices.size)
        assertEquals("KSeF-1", result.invoices.first().ksefNumber)
    }

    @Test
    fun `listInvoices rejects invalid page size`() {
        val repository = mockk<KsefRepository>()
        val invoiceService = mockk<InvoiceService>()
        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val invoiceRepo = mockk<InvoiceRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo, invoiceRepo)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.listInvoices(pageSize = 5) }
        }
    }

    @Test
    fun `sendInvoiceById fails when invoice not found`() {
        val repository = mockk<KsefRepository>()
        val invoiceService = mockk<InvoiceService>()
        coEvery { invoiceService.getInvoiceById(99L) } returns null

        val ksefSessionInvoiceStatusRepo = mockk<KsefSessionInvoiceStatusRepo>(relaxed = true)
        val invoiceRepo = mockk<InvoiceRepo>(relaxed = true)
        val service = KsefServiceImpl(config, repository, invoiceService, ksefSessionInvoiceStatusRepo, invoiceRepo)

        val ex = assertThrows(KsefException::class.java) {
            runBlocking { service.sendInvoiceById(99L) }
        }
        assertEquals(404, ex.statusCode)
    }

    @Test
    fun `login fails when token is missing`() {
        val service = KsefServiceImpl(
            config.copy(token = null),
            mockk(),
            mockk(),
            mockk(relaxed = true),
            mockk(relaxed = true),
        )

        assertThrows(KsefException::class.java) {
            runBlocking { service.login() }
        }
    }

    companion object {
        private const val TEST_CERTIFICATE_BASE64 =
            "MIIDBzCCAe+gAwIBAgIUGao5uR6TgnLKrRUWonXnf8eh6r0wDQYJKoZIhvcNAQELBQAwEzERMA8GA1UEAwwIS1NlRlRlc3QwHhcNMjYwNTIxMDk0MDE0WhcNMjcwNTIxMDk0MDE0WjATMREwDwYDVQQDDAhLU2VGVGVzdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALe+KoOiOaUXH6JdQdu4YMLrnSkU4NHK8FMKO8vx009xO777YWgNLRx1vf+yneUBMgZWD20MC1/oBsi+EYpw237kv7wjmHUjA7UABH/SWJuoIRPFh2kgoI3mtvyVseG4RRojEVfc8p1NmUOkcnr31zFKNrYJ6V2iW+DOFYUaBaiDfri9ZNUWgfOLVQhX5gsGtTaVMleKskdQ5/ggjpMkWBQ/juYAgzAFkG7Kdf1AXVL27tiGr0er6R+oM+cgeJAxsDK4x0itFetxzKSnZROBkEO2gfSUymfmDZIJeNiQ4BhUQZWxJ14ivp8OjAKOnOgdpSsXzrkYDhT0rTMsvzSnnF0CAwEAAaNTMFEwHQYDVR0OBBYEFO6RnO86QivOF1R83CCaHEXyh3ehMB8GA1UdIwQYMBaAFO6RnO86QivOF1R83CCaHEXyh3ehMA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEBAGQimdtmBoFM9DlxneSUXpWeWZjg5fk96VgqTH25taVz1dGh3zIKhIDzS4h3nKjhDGfPOHzH2nfCfr8q30cIZoxLBP1iQ30djRapTI3WXQ69dtKRWY7lHDpQz0Cb8xxws2y8WSN7RLur+73rkopBxPY+sWYf3MHNJXdOBX9Py29MiEXJ2K+s74EObxqC8pjpCVi6zY8Wy5krCAGK6OlHue8srlXMB70w/YFcbNZI3earvdj7k/xYSgTBrwG2uSqKXZ5AnD4dbYchi+Tr70K2LIgVXVg3aSUagbuMe2QWhP/bR7t/uPLdPrwjg9DnxbhHlNSEEF6xbap/cg45cAYf35U="
    }
}
