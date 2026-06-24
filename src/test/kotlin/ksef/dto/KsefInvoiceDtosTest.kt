package com.kontenery.ksef.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KsefInvoiceDtosTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `deserializes query metadata response with buyer identifier object`() {
        val response = json.decodeFromString<KsefQueryInvoiceMetadataResponse>(
            """
            {
              "hasMore": false,
              "invoices": [
                {
                  "ksefNumber": "5555555555-20250828-010080615740-E4",
                  "invoiceNumber": "29/6/2026",
                  "buyer": {
                    "identifier": {
                      "type": "Nip",
                      "value": "8822055123"
                    },
                    "name": "Test Buyer"
                  }
                }
              ]
            }
            """.trimIndent(),
        )

        val invoice = response.invoices.single()
        assertEquals("29/6/2026", invoice.invoiceNumber)
        assertEquals("Nip", invoice.buyer?.identifier?.type)
        assertEquals("8822055123", invoice.buyer?.identifier?.value)
        assertEquals("Test Buyer", invoice.buyer?.name)
    }
}
