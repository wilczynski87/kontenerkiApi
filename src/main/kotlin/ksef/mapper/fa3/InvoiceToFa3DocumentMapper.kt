package com.kontenery.ksef.mapper.fa3

import com.kontenery.data.Address
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Subject
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator.LineAmounts
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator.VatBucket
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator.formatAmount
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator.groupVatBuckets
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator.lineAmounts
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.util.UUID

/**
 * Maps domain [Invoice] to [Fa3InvoiceDocument] (business layer, no XML).
 * Structure follows CIRFMF `invoice-template_v3.xml` sections.
 */
object InvoiceToFa3DocumentMapper {

    fun map(invoice: Invoice): Fa3InvoiceDocument {
        validate(invoice)
        val issueDate = invoice.invoiceDate!!
        val lines = invoice.products.map { lineAmounts(it) }
        val buckets = groupVatBuckets(lines)
        if (buckets.size > Fa3Constants.MAX_VAT_SUMMARY_SLOTS) {
            throw KsefException(
                "Too many VAT rates on invoice for KSeF (max ${Fa3Constants.MAX_VAT_SUMMARY_SLOTS})",
            )
        }

        val seller = invoice.seller!!
        val customer = invoice.customer!!
        val gross = buckets.fold(BigDecimal.ZERO) { acc, b -> acc.add(b.net).add(b.vat) }

        return Fa3InvoiceDocument(
            header = Fa3Header(
                issueDate = issueDate,
                productionDateTime = "${formatDate(issueDate)}T00:00:00Z",
            ),
            seller = toParty(seller),
            buyer = toBuyer(customer),
            body = Fa3Body(
                issueDate = issueDate,
                invoiceNumber = invoice.invoiceNumber!!.trim(),
                saleDate = issueDate,
                vatSummaries = buckets.mapIndexed { index, bucket -> toSummarySlot(index, bucket) },
                grossTotal = formatAmount(gross),
                lines = lines.mapIndexed { index, line -> toLine(index, line) },
                payment = Fa3Payment(
                    paid = Fa3Constants.PAYMENT_NOT_PAID,
                    dueDate = invoice.paymentDay ?: issueDate,
                    paymentForm = Fa3Constants.PAYMENT_FORM_TRANSFER,
                    bankAccountDigits = invoice.mainAccount
                        .takeIf { it.isNotBlank() }
                        ?.filter { it.isDigit() }
                        ?.takeIf { it.isNotEmpty() },
                ),
            ),
        )
    }

    private fun validate(invoice: Invoice) {
        require(!invoice.invoiceNumber.isNullOrBlank()) { "invoiceNumber is required for KSeF" }
        require(invoice.invoiceDate != null) { "invoiceDate is required for KSeF" }
        require(invoice.seller != null) { "seller is required for KSeF" }
        require(invoice.customer != null) { "customer is required for KSeF" }
        require(invoice.products.isNotEmpty()) { "invoice must have at least one position for KSeF" }
        requireNip(invoice.seller!!.nip, "seller")
        requireNip(invoice.customer!!.nip, "customer")
    }

    private fun requireNip(nip: String?, party: String) {
        val digits = digitsOnly(nip)
        require(digits.length == 10) { "$party NIP must have exactly 10 digits for KSeF" }
    }

    private fun toParty(seller: Subject.Seller): Fa3Party = Fa3Party(
        nip = digitsOnly(seller.nip),
        name = seller.name,
        address = toAddress(seller.address),
        email = seller.email.trim(),
        phoneDigits = seller.phone?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }?.take(16),
    )

    private fun toBuyer(customer: Subject.Customer): Fa3Buyer = Fa3Buyer(
        nip = digitsOnly(customer.nip),
        name = customer.name,
        address = toAddress(customer.address),
        email = customer.email.trim(),
        phoneDigits = customer.phone?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }?.take(16),
        clientNumber = customer.invoiceNumber?.takeIf { it.isNotBlank() },
    )

    private fun toAddress(address: Address?): Fa3Address {
        val normalized = normalizeAddress(address ?: Address())
        val line1 = listOfNotNull(normalized.street?.trim(), normalized.house?.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .ifBlank { "brak" }
        val postCode = normalized.postCode?.trim().orEmpty()
        val city = normalized.city?.trim().orEmpty()
        val line2 = when {
            postCode.isNotEmpty() && city.isNotEmpty() -> "$postCode $city"
            postCode.isNotEmpty() -> postCode
            city.isNotEmpty() -> city
            else -> "brak"
        }
        return Fa3Address(
            countryCode = normalized.country.ifBlank { "PL" },
            line1 = line1,
            line2 = line2,
        )
    }

    private fun toSummarySlot(index: Int, bucket: VatBucket): Fa3VatSummarySlot {
        val slot = index + 1
        return Fa3VatSummarySlot(
            slotIndex = slot,
            netAmount = formatAmount(bucket.net),
            vatAmount = if (bucket.rate in Fa3Constants.VAT_RATES_WITH_TAX) {
                formatAmount(bucket.vat)
            } else {
                null
            },
            vatRateKey = bucket.rate,
        )
    }

    private fun toLine(index: Int, line: LineAmounts): Fa3Line = Fa3Line(
        rowNumber = index + 1,
        lineUuid = UUID.randomUUID().toString(),
        productName = line.productName,
        unit = "szt.",
        quantity = formatAmount(line.quantity),
        unitPriceNet = formatAmount(line.unitPrice),
        netAmount = formatAmount(line.net),
        vatRate = line.vatRate,
    )

    private fun normalizeAddress(address: Address): Address {
        val city = address.city?.trim().orEmpty()
        val postCode = address.postCode?.trim().orEmpty()
        val postalPattern = Regex("^\\d{2}-\\d{3}$")
        return if (city.matches(postalPattern) && postCode.isNotEmpty() && !postCode.matches(postalPattern)) {
            address.copy(city = postCode, postCode = city)
        } else {
            address
        }
    }

    private fun formatDate(date: LocalDate): String =
        "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"

    private fun digitsOnly(nip: String?): String =
        nip?.filter { it.isDigit() }.orEmpty()
}
