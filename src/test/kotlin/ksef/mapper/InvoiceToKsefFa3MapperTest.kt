package com.kontenery.ksef.mapper

import com.kontenery.data.Address
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceToKsefFa3MapperTest {

    @Test
    fun `toFa3Xml maps seller customer and positions`() {
        val invoice = sampleInvoice()
        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)

        assertTrue(xml.contains("<NIP>8943278612</NIP>"))
        assertTrue(xml.contains("<Nazwa>Kontenery Magazynowe sp z o.o.</Nazwa>"))
        assertTrue(xml.contains("<NIP>1234567890</NIP>"))
        assertTrue(xml.contains("<P_2>FV/1/2025</P_2>"))
        assertTrue(xml.contains("<P_7>Wynajem kontenera</P_7>"))
        assertTrue(xml.contains("<RodzajFaktury>VAT</RodzajFaktury>"))
    }

    @Test
    fun `toFa3Xml places FaWiersz before summary and Adnotacje`() {
        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(sampleInvoice())
        val faStart = xml.indexOf("<Fa>")
        val faEnd = xml.indexOf("</Fa>")
        val faSection = xml.substring(faStart, faEnd)
        assertTrue(faSection.indexOf("<FaWiersz>") < faSection.indexOf("<P_13_1>"))
        assertTrue(faSection.indexOf("<P_13_1>") < faSection.indexOf("<Adnotacje>"))
        assertTrue(faSection.indexOf("<Adnotacje>") < faSection.indexOf("<RodzajFaktury>"))
        assertTrue(faSection.indexOf("<RodzajFaktury>") < faSection.indexOf("<Platnosc>"))
    }

    @Test
    fun `toFa3Xml fixes swapped seller postCode and city`() {
        val invoice = sampleInvoice().copy(
            seller = Subject.Seller.company("FV/1/2025").copy(
                address = Address(street = "ul. Test", house = "1", city = "53-238", postCode = "Wrocław"),
            ),
        )
        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)
        assertTrue(xml.contains("<AdresL2>53-238 Wrocław</AdresL2>"))
    }

    @Test
    fun `toFa3Xml requires invoice number`() {
        assertThrows<IllegalArgumentException> {
            InvoiceToKsefFa3Mapper.toFa3Xml(sampleInvoice().copy(invoiceNumber = null))
        }
    }

    @Test
    fun `normalizeVatRate converts decimal fraction to catalog percent`() {
        assertEquals("23", InvoiceToKsefFa3Mapper.normalizeVatRate("0.23"))
        assertEquals("8", InvoiceToKsefFa3Mapper.normalizeVatRate("0.08"))
        assertEquals("23", InvoiceToKsefFa3Mapper.normalizeVatRate("23.00"))
    }

    @Test
    fun `toFa3Xml uses catalog P_12 and consistent P_13 P_14 P_15`() {
        val invoice = sampleInvoice().copy(
            products = listOf(
                Position(
                    productName = "Energia",
                    unitPrice = "100.00",
                    quantity = "1",
                    price = "100.00",
                    vatRate = "0.23",
                    vatAmount = "23.00",
                    priceWithVat = "123.00",
                ),
            ),
        )
        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)
        assertTrue(xml.contains("<P_12>23</P_12>"))
        assertTrue(xml.contains("<P_13_1>100.00</P_13_1>"))
        assertTrue(xml.contains("<P_14_1>23.00</P_14_1>"))
        assertTrue(xml.contains("<P_15>123.00</P_15>"))
    }

    @Test
    fun `toFa3Xml splits summary by VAT rate`() {
        val invoice = sampleInvoice().copy(
            products = listOf(
                Position(
                    productName = "Kontener",
                    unitPrice = "100.00",
                    quantity = "1",
                    price = "100.00",
                    vatRate = "23",
                    vatAmount = "23.00",
                    priceWithVat = "123.00",
                ),
                Position(
                    productName = "Woda",
                    unitPrice = "50.00",
                    quantity = "1",
                    price = "50.00",
                    vatRate = "8",
                    vatAmount = "4.00",
                    priceWithVat = "54.00",
                ),
            ),
        )
        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)
        assertTrue(xml.contains("<P_13_1>100.00</P_13_1>"))
        assertTrue(xml.contains("<P_14_1>23.00</P_14_1>"))
        assertTrue(xml.contains("<P_13_2>50.00</P_13_2>"))
        assertTrue(xml.contains("<P_14_2>4.00</P_14_2>"))
        assertTrue(xml.contains("<P_15>177.00</P_15>"))
    }

    private fun sampleInvoice(): Invoice = Invoice(
        invoiceNumber = "FV/1/2025",
        invoiceDate = LocalDate(2025, 5, 15),
        paymentDay = LocalDate(2025, 5, 29),
        seller = Subject.Seller.company("FV/1/2025"),
        customer = Subject.Customer(
            name = "Test Klient Sp. z o.o.",
            address = Address(street = "Testowa", house = "1", city = "Wrocław", postCode = "50-001"),
            nip = "1234567890",
            email = "klient@example.com",
            phone = "123456789",
        ),
        products = listOf(
            Position(
                productName = "Wynajem kontenera",
                unitPrice = "100.00",
                quantity = "1",
                price = "100.00",
                vatRate = "23",
                vatAmount = "23.00",
                priceWithVat = "123.00",
            ),
        ),
        vatAmountSum = "23.00",
        priceSum = "100.00",
        priceWithVatSum = "123.00",
    )
}
