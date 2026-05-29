package com.kontenery.ksef.mapper

import com.kontenery.data.Contract
import com.kontenery.data.Product
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import java.math.BigDecimal
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Reproduces periodic invoice positions built like ContractServiceImpl (vatRate = 0.23).
 */
class InvoiceToKsefFa3MapperContractBugTest {

    @Test
    fun `toFa3Xml handles contract vatRate stored as 0_23 fraction`() {
        val contract = Contract(
            netPrice = BigDecimal("100.00"),
            vatRate = BigDecimal("0.23"),
            product = Product.Container(id = 1, name = "Kontener 6m"),
        )
        val position = Position.toPosition(contract)
        val invoice = periodicInvoice(listOf(position))

        val xml = InvoiceToKsefFa3Mapper.toFa3Xml(invoice)
        assertTrue(xml.contains("<P_12>23</P_12>"), "P_12 catalog value")
        assertTrue(xml.contains("<P_14_1>23.00</P_14_1>"), "VAT must match 23% of net, not 0.23")
        assertTrue(xml.contains("<P_15>123.00</P_15>"))
    }

    private fun periodicInvoice(products: List<Position>): Invoice = Invoice(
        invoiceNumber = "FV/TEST/2025",
        invoiceDate = LocalDate(2025, 5, 1),
        paymentDay = LocalDate(2025, 5, 15),
        seller = Subject.Seller.company("FV/TEST/2025"),
        customer = Subject.Customer(
            name = "Test Sp. z o.o.",
            nip = "5261040828",
            email = "test@example.com",
        ),
        products = products,
        vatApply = true,
        mainAccount = "51 1870 1045 2078 1089 5944 0001",
    )
}
