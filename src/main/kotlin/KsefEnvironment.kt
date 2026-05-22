package com.kontenery

/**
 * Oficjalne środowiska KSeF API 2.0 (MF).
 * W trybie DEV domyślnie używane jest [TEST] (sandbox integracyjny).
 */
enum class KsefEnvironment(
    val baseUrl: String,
    val apiSuffix: String = "v2",
) {
    TEST("https://api-test.ksef.mf.gov.pl"),
    DEMO("https://api-demo.ksef.mf.gov.pl"),
    PRODUCTION("https://api.ksef.mf.gov.pl"),
    ;

    val docsUrl: String
        get() = "${baseUrl.trimEnd('/')}/docs/$apiSuffix"

    companion object {
        /** Domyślne środowisko przy API_ENV=DEV. */
        val DEV_DEFAULT: KsefEnvironment = TEST

        fun fromEnvValue(value: String?): KsefEnvironment? {
            if (value.isNullOrBlank()) return null
            return when (value.trim().uppercase()) {
                "TEST", "TE", "SANDBOX", "RC" -> TEST
                "DEMO", "PREPROD", "PRE-PROD" -> DEMO
                "PROD", "PRD", "PRODUCTION" -> PRODUCTION
                else -> null
            }
        }
    }
}
