package com.kontenery.ksef.service

import com.kontenery.ksef.client.KsefApiClient
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.repository.impl.KsefRepositoryImpl
import com.kontenery.ksef.service.impl.KsefServiceImpl
import com.kontenery.resolveKsefConfig
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

/**
 * Live login against KSeF TEST (sandbox). Skipped when [KSEF_TOKEN] is unset.
 * Run: `KSEF_ENV=TEST KSEF_TOKEN=... ./gradlew test --tests KsefLoginIntegrationTest`
 */
class KsefLoginIntegrationTest {

    @Test
    fun `login authenticates against KSeF TEST sandbox`() = runBlocking {
        val config = resolveKsefConfig(
            apiEnv = "DEV",
            getenv = { System.getenv(it) },
        )
        assumeTrue(!config.token.isNullOrBlank(), "KSEF_TOKEN not set — skipping live KSeF login test")
        assumeTrue(
            config.baseUrl.contains("api-test"),
            "Refusing live login outside TEST sandbox (baseUrl=${config.baseUrl})",
        )

        val service = KsefServiceImpl(
            config = config,
            repository = KsefRepositoryImpl(KsefApiClient(config)),
            invoiceService = mockk(relaxed = true),
            ksefSessionInvoiceStatusRepo = mockk(relaxed = true),
        )

        val result = runCatching { service.login() }.getOrElse { error ->
            val message = (error as? KsefException)?.message ?: error.message.orEmpty()
            if (
                message.contains("placeholder", ignoreCase = true) ||
                message.contains("450") ||
                message.contains("błędnego tokenu", ignoreCase = true) ||
                message.contains("invalid or expired", ignoreCase = true)
            ) {
                assumeTrue(
                    false,
                    "KSEF_TOKEN not usable for live login (placeholder, invalid, or expired) — set a fresh token from KSeF TEST portal for NIP ${config.nip}",
                )
            }
            throw error
        }

        assertTrue(result.authenticated)
        assertNotNull(result.validUntil)
    }
}
