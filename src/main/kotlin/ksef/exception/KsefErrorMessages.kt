package com.kontenery.ksef.exception

/**
 * User-facing KSeF messages without tokens, full invoice payloads, or other sensitive data.
 */
object KsefErrorMessages {

    fun userMessage(exception: KsefException): String {
        val sanitized = sanitize(exception.message)
        if (sanitized.isBlank()) return defaultMessage(exception)

        // Keep the original (sanitized) message unless we intentionally replace it for clarity/security.
        return when {
            sanitized.contains("authentication failed", ignoreCase = true) &&
                !sanitized.contains("KSEF_TOKEN", ignoreCase = true) ->
                "KSeF authentication failed. Check KSEF_TOKEN, KSEF_NIP and environment (TEST/DEMO/PRODUCTION)."
            else -> sanitized
        }
    }

    private fun defaultMessage(exception: KsefException): String = when (exception.statusCode) {
        404 -> "Invoice not found"
        else -> "KSeF operation failed"
    }

    private fun sanitize(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace(Regex("""Bearer\s+[A-Za-z0-9._-]+""", RegexOption.IGNORE_CASE), "Bearer [redacted]")
            .replace(Regex("""Problem to sendInvoiceToKsef:\s*""", RegexOption.IGNORE_CASE), "KSeF invoice send failed: ")
            .replace(Regex("""invoice:\s*\{[\s\S]*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(500)
    }
}
