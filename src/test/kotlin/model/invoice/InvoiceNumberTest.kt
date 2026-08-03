package com.kontenery.model.invoice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InvoiceNumberTest {

    @Test
    fun `parses VAT invoice number`() {
        val parsed = InvoiceNumber.toInvoiceNumber("40/8/2026")
        assertEquals(40L, parsed.number)
        assertEquals(8, parsed.month)
        assertEquals("2026", parsed.year)
        assertEquals("40/8/2026", parsed.toInvoiceNumberString())
    }

    @Test
    fun `parses bill number stripping trailing r from year`() {
        val parsed = InvoiceNumber.toInvoiceNumber("26/8/2026r")
        assertEquals(26L, parsed.number)
        assertEquals(8, parsed.month)
        assertEquals("2026", parsed.year)
        assertEquals("26/8/2026", parsed.toInvoiceNumberString())
    }
}
