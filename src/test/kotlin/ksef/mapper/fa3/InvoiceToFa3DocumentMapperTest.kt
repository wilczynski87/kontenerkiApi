package com.kontenery.ksef.mapper.fa3

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import com.kontenery.ksef.mapper.InvoiceToKsefFa3Mapper
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InvoiceToFa3DocumentMapperTest {

    @Test
    fun `map builds document matching xml writer output`() {
        val invoice = Invoice(
            invoiceNumber = "FV/1/2025",
            invoiceDate = LocalDate(2025, 5, 15),
            seller = Subject.Seller.company("FV/1/2025"),
            customer = Subject.Customer(
                name = "Test",
                nip = "5261040828",
                email = "a@b.pl",
            ),
            products = listOf(
                Position(
                    productName = "Usługa",
                    unitPrice = "100",
                    quantity = "1",
                    price = "100",
                    vatRate = "23",
                    vatAmount = "23",
                    priceWithVat = "123",
                ),
            ),
            mainAccount = "12 3456",
        )

        val document = InvoiceToFa3DocumentMapper.map(invoice)
        val fromDocument = Fa3InvoiceXmlWriter.write(document)
        val fromFacade = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)

        assertEquals(fromFacade, fromDocument)
        assertEquals("23", document.body.lines.single().vatRate)
        assertEquals(1, document.body.vatSummaries.single().slotIndex)
    }
}
