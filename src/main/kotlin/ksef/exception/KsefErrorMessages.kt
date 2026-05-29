package com.kontenery.ksef.exception

/**
 * User-facing KSeF messages without tokens, full invoice payloads, or other sensitive data.
 */
object KsefErrorMessages {

    fun userMessage(exception: KsefException): String {
        val sanitized = sanitize(exception.message)
        if (sanitized.isBlank()) return defaultMessage(exception)

        return when {
            sanitized.contains("invoice processing failed", ignoreCase = true) -> sanitized
            sanitized.contains("authentication failed", ignoreCase = true) ->
                "KSeF authentication failed. Check KSEF_TOKEN, KSEF_NIP and environment (TEST/DEMO/PRODUCTION)."
            sanitized.contains("authentication timed out", ignoreCase = true) -> sanitized
            sanitized.contains("processing timed out", ignoreCase = true) -> sanitized
            sanitized.startsWith("Invoice not found", ignoreCase = true) -> sanitized
            sanitized.contains("is not set", ignoreCase = true) -> sanitized
            sanitized.contains("Invalid decimal", ignoreCase = true) -> sanitized
            sanitized.contains("is required for KSeF", ignoreCase = true) -> sanitized
            sanitized.contains("invoice send failed", ignoreCase = true) -> sanitized
            sanitized.startsWith("KSeF API error:", ignoreCase = true) -> sanitized
            sanitized.startsWith("KSeF", ignoreCase = true) -> sanitized
            else -> defaultMessage(exception)
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
            // Legacy message format used to include a full invoice dump - keep the error but drop the payload.
            .replace(Regex("""Problem to sendInvoiceToKsef:\s*""", RegexOption.IGNORE_CASE), "KSeF invoice send failed: ")
            .replace(Regex("""invoice:\s*\{[\s\S]*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .take(500)
    }
}
