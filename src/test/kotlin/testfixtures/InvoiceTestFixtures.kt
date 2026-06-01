package com.kontenery.testfixtures

import com.kontenery.data.Address
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import kotlinx.datetime.LocalDate

fun sampleVatInvoice(
    invoiceNumber: String = "FV/1/2025",
    vatApply: Boolean = true,
): Invoice = Invoice(
    invoiceNumber = invoiceNumber,
    invoiceDate = LocalDate(2025, 5, 15),
    paymentDay = LocalDate(2025, 5, 29),
    seller = Subject.Seller.company(invoiceNumber),
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
    vatApply = vatApply,
)

fun sampleNonVatInvoice(invoiceNumber: String = "R/1/2025"): Invoice =
    sampleVatInvoice(invoiceNumber = invoiceNumber, vatApply = false)
