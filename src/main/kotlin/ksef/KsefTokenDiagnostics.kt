package com.kontenery.ksef

/**
 * Token KSeF 2.0: `{prefix}|nip-{NIP}|{secret}` (pipe-separated).
 */
internal object KsefTokenDiagnostics {

    private val TOKEN_NIP_REGEX = Regex("""\|nip-(\d{10})\|""")

    /** Example hash from .env.example — not valid for authentication. */
    private const val PLACEHOLDER_SECRET_SUFFIX = "4bbbb1a9a28c4cd493077bf1057d3b34b87f8ba8ba6d4bec9e43d840110c0f74"

    fun nipFromToken(token: String): String? =
        TOKEN_NIP_REGEX.find(token)?.groupValues?.get(1)

    fun segmentCount(token: String): Int = token.count { it == '|' } + 1

    fun isPlaceholderExampleToken(token: String): Boolean =
        token.contains(PLACEHOLDER_SECRET_SUFFIX)

    fun validateForAuthentication(token: String, configuredNip: String?) {
        val segments = segmentCount(token)
        if (segments < 3) {
            throw com.kontenery.ksef.exception.KsefException(
                "KSEF_TOKEN looks truncated (expected 3 pipe-separated parts, got $segments). " +
                    "Use quotes in .env: KSEF_TOKEN=\"...|nip-...|...\" or KSEF_TOKEN_FILE=/path/to/token.txt",
            )
        }
        if (isPlaceholderExampleToken(token)) {
            throw com.kontenery.ksef.exception.KsefException(
                "KSEF_TOKEN is the placeholder from .env.example — generate a new system token in the KSeF TEST portal " +
                    "(MCU) for NIP ${configuredNip ?: "?"} and set KSEF_TOKEN or KSEF_TOKEN_FILE in .env",
            )
        }
        val tokenNip = nipFromToken(token)
        val nip = configuredNip?.trim()?.takeIf { it.isNotEmpty() }
        if (tokenNip != null && nip != null && tokenNip != nip) {
            throw com.kontenery.ksef.exception.KsefException(
                "KSEF_TOKEN was issued for NIP $tokenNip but KSEF_NIP=$nip — they must match",
            )
        }
    }
}
