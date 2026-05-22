package com.kontenery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KsefConfigResolutionTest {

    @Test
    fun `DEV defaults to TEST sandbox`() {
        val config = resolveKsefConfig(
            apiEnv = "DEV",
            getenv = { null },
        )
        assertEquals("TEST", config.environment)
        assertEquals("https://api-test.ksef.mf.gov.pl", config.baseUrl)
        assertEquals("v2", config.apiSuffix)
        assertEquals(KSEF_DEV_DEFAULT_NIP, config.nip)
    }

    @Test
    fun `DEV rejects production KSeF URL`() {
        assertThrows(IllegalStateException::class.java) {
            resolveKsefConfig(
                apiEnv = "DEV",
                getenv = { name ->
                    when (name) {
                        "KSEF_BASE_URL" -> "https://api.ksef.mf.gov.pl"
                        else -> null
                    }
                },
            )
        }
    }

    @Test
    fun `KSEF_ENV DEMO selects demo API`() {
        val config = resolveKsefConfig(
            apiEnv = "DEV",
            getenv = { name -> if (name == "KSEF_ENV") "DEMO" else null },
        )
        assertEquals("DEMO", config.environment)
        assertEquals("https://api-demo.ksef.mf.gov.pl", config.baseUrl)
    }

    @Test
    fun `production env defaults to PRD when not DEV`() {
        val config = resolveKsefConfig(
            apiEnv = "PROD",
            getenv = { null },
        )
        assertEquals("PRODUCTION", config.environment)
        assertEquals("https://api.ksef.mf.gov.pl", config.baseUrl)
    }

    @Test
    fun `KSEF_TOKEN_FILE is read when KSEF_TOKEN unset`() {
        val tokenFile = java.nio.file.Files.createTempFile("ksef-token", ".txt").toFile()
        tokenFile.writeText("  token-from-file  ")
        try {
            val config = resolveKsefConfig(
                apiEnv = "DEV",
                getenv = { name ->
                    when (name) {
                        "KSEF_TOKEN_FILE" -> tokenFile.absolutePath
                        else -> null
                    }
                },
            )
            assertEquals("token-from-file", config.token)
        } finally {
            tokenFile.delete()
        }
    }

    @Test
    fun `SANDBOX alias maps to TEST`() {
        val config = resolveKsefConfig(
            apiEnv = "PROD",
            getenv = { name -> if (name == "KSEF_ENV") "SANDBOX" else null },
        )
        assertEquals("TEST", config.environment)
        assertTrue(config.baseUrl.contains("api-test"))
    }
}
