package com.kontenery.ksef.mapper

import com.kontenery.data.Address
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Position
import com.kontenery.data.invoice.Subject
import com.kontenery.ksef.exception.KsefException
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

object InvoiceToKsefFa3Mapper {

    private val VAT_RATES_WITH_TAX = setOf("23", "22", "8", "7", "5", "4", "3")
    private val MAX_VAT_SUMMARY_SLOTS = 5

    fun toFa3Xml(invoice: Invoice): String {
        validate(invoice)
        val seller = invoice.seller!!
        val customer = invoice.customer!!
        val invoiceNumber = invoice.invoiceNumber!!.trim()
        val issueDate = invoice.invoiceDate!!
        val saleDate = invoice.paymentDay ?: issueDate
        val lines = invoice.products.map { lineAmounts(it) }
        val vatBuckets = groupVatBuckets(lines)

        return buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine(
                """<Faktura xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" """ +
                    """xmlns:xsd="http://www.w3.org/2001/XMLSchema" """ +
                    """xmlns:etd="http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/" """ +
                    """xmlns="http://crd.gov.pl/wzor/2025/06/25/13775/">""",
            )
            appendLine("    <Naglowek>")
            appendLine("""        <KodFormularza kodSystemowy="FA (3)" wersjaSchemy="1-0E">FA</KodFormularza>""")
            appendLine("        <WariantFormularza>3</WariantFormularza>")
            appendLine("        <DataWytworzeniaFa>${formatDate(issueDate)}T00:00:00Z</DataWytworzeniaFa>")
            appendLine("        <SystemInfo>Kontenerki</SystemInfo>")
            appendLine("    </Naglowek>")
            appendPodmiot1(seller)
            appendPodmiot2(customer)
            appendLine("    <Fa>")
            appendLine("        <KodWaluty>PLN</KodWaluty>")
            appendLine("        <P_1>${formatDate(issueDate)}</P_1>")
            appendLine("        <P_2>${escapeXml(invoiceNumber)}</P_2>")
            appendLine("        <P_6>${formatDate(saleDate)}</P_6>")
            lines.forEachIndexed { index, line ->
                appendFaWiersz(index + 1, line)
            }
            appendVatSummaries(vatBuckets)
            appendStandardAnnotations()
            appendLine("        <RodzajFaktury>VAT</RodzajFaktury>")
            appendPayment(invoice)
            appendLine("    </Fa>")
            appendLine("</Faktura>")
        }
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

    private fun StringBuilder.appendPodmiot1(seller: Subject.Seller) {
        appendLine("    <Podmiot1>")
        appendLine("        <DaneIdentyfikacyjne>")
        appendLine("            <NIP>${digitsOnly(seller.nip)}</NIP>")
        appendLine("            <Nazwa>${escapeXml(seller.name)}</Nazwa>")
        appendLine("        </DaneIdentyfikacyjne>")
        appendAddress(seller.address)
        appendContact(seller.email, seller.phone)
        appendLine("    </Podmiot1>")
    }

    private fun StringBuilder.appendPodmiot2(customer: Subject.Customer) {
        appendLine("    <Podmiot2>")
        appendLine("        <DaneIdentyfikacyjne>")
        appendLine("            <NIP>${digitsOnly(customer.nip)}</NIP>")
        appendLine("            <Nazwa>${escapeXml(customer.name)}</Nazwa>")
        appendLine("        </DaneIdentyfikacyjne>")
        appendAddress(customer.address)
        appendContact(customer.email, customer.phone)
        customer.invoiceNumber?.takeIf { it.isNotBlank() }?.let {
            appendLine("        <NrKlienta>${escapeXml(it)}</NrKlienta>")
        }
        appendLine("        <JST>2</JST>")
        appendLine("        <GV>2</GV>")
        appendLine("    </Podmiot2>")
    }

    private fun StringBuilder.appendAddress(address: Address?) {
        val addr = normalizeAddress(address ?: Address())
        val line1 = listOfNotNull(addr.street?.trim(), addr.house?.trim())
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .ifBlank { "brak" }
        val postCode = addr.postCode?.trim().orEmpty()
        val city = addr.city?.trim().orEmpty()
        val line2 = when {
            postCode.isNotEmpty() && city.isNotEmpty() -> "$postCode $city"
            postCode.isNotEmpty() -> postCode
            city.isNotEmpty() -> city
            else -> "brak"
        }
        appendLine("        <Adres>")
        appendLine("            <KodKraju>${escapeXml(addr.country.ifBlank { "PL" })}</KodKraju>")
        appendLine("            <AdresL1>${escapeXml(line1)}</AdresL1>")
        appendLine("            <AdresL2>${escapeXml(line2)}</AdresL2>")
        appendLine("        </Adres>")
    }

    private fun StringBuilder.appendContact(email: String, phone: String?) {
        appendLine("        <DaneKontaktowe>")
        appendLine("            <Email>${escapeXml(email.trim())}</Email>")
        phone?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }?.take(16)?.let {
            appendLine("            <Telefon>${escapeXml(it)}</Telefon>")
        }
        appendLine("        </DaneKontaktowe>")
    }

    private fun StringBuilder.appendStandardAnnotations() {
        appendLine("        <Adnotacje>")
        appendLine("            <P_16>2</P_16>")
        appendLine("            <P_17>2</P_17>")
        appendLine("            <P_18>2</P_18>")
        appendLine("            <P_18A>2</P_18A>")
        appendLine("            <Zwolnienie><P_19N>1</P_19N></Zwolnienie>")
        appendLine("            <NoweSrodkiTransportu><P_22N>1</P_22N></NoweSrodkiTransportu>")
        appendLine("            <P_23>2</P_23>")
        appendLine("            <PMarzy><P_PMarzyN>1</P_PMarzyN></PMarzy>")
        appendLine("        </Adnotacje>")
    }

    private fun StringBuilder.appendFaWiersz(rowNumber: Int, line: LineAmounts) {
        appendLine("        <FaWiersz>")
        appendLine("            <NrWierszaFa>$rowNumber</NrWierszaFa>")
        appendLine("            <UU_ID>${UUID.randomUUID()}</UU_ID>")
        appendLine("            <P_7>${escapeXml(line.productName)}</P_7>")
        appendLine("            <P_8A>szt.</P_8A>")
        appendLine("            <P_8B>${formatAmount(line.quantity)}</P_8B>")
        appendLine("            <P_9A>${formatAmount(line.unitPrice)}</P_9A>")
        appendLine("            <P_11>${formatAmount(line.net)}</P_11>")
        appendLine("            <P_12>${escapeXml(line.vatRate)}</P_12>")
        appendLine("        </FaWiersz>")
    }

    private fun StringBuilder.appendVatSummaries(buckets: List<VatBucket>) {
        if (buckets.size > MAX_VAT_SUMMARY_SLOTS) {
            throw KsefException("Too many VAT rates on invoice for KSeF (max $MAX_VAT_SUMMARY_SLOTS)")
        }
        var gross = BigDecimal.ZERO
        buckets.forEachIndexed { index, bucket ->
            val slot = index + 1
            appendLine("        <P_13_$slot>${formatAmount(bucket.net)}</P_13_$slot>")
            if (bucket.rate in VAT_RATES_WITH_TAX) {
                appendLine("        <P_14_$slot>${formatAmount(bucket.vat)}</P_14_$slot>")
            }
            gross = gross.add(bucket.net).add(bucket.vat)
        }
        appendLine("        <P_15>${formatAmount(gross)}</P_15>")
    }

    private fun StringBuilder.appendPayment(invoice: Invoice) {
        val paymentDate = invoice.paymentDay ?: invoice.invoiceDate!!
        appendLine("        <Platnosc>")
        appendLine("            <Zaplacono>2</Zaplacono>")
        appendLine("            <TerminPlatnosci>${formatDate(paymentDate)}</TerminPlatnosci>")
        appendLine("            <FormaPlatnosci>6</FormaPlatnosci>")
        invoice.mainAccount.takeIf { it.isNotBlank() }?.let { account ->
            val digits = account.filter { it.isDigit() }
            if (digits.isNotEmpty()) {
                appendLine("            <RachunekBankowy>")
                appendLine("                <NrRB>$digits</NrRB>")
                appendLine("            </RachunekBankowy>")
            }
        }
        appendLine("        </Platnosc>")
    }

    private fun lineAmounts(position: Position): LineAmounts {
        val quantity = parseDecimal(position.quantity, "quantity")
        val unitPrice = parseDecimal(position.unitPrice, "unitPrice")
        val declaredNet = parseDecimal(position.price, "price")
        val computedNet = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
        val net = if (declaredNet.subtract(computedNet).abs() <= BigDecimal("0.01")) {
            declaredNet.setScale(2, RoundingMode.HALF_UP)
        } else {
            computedNet
        }
        val vatRate = normalizeVatRate(position.vatRate)
        val computedVat = vatFromNet(net, vatRate)
        val declaredVat = position.vatAmount?.let { parseDecimal(it, "vatAmount") }
        val vat = declaredVat?.let { declared ->
            if (declared.subtract(computedVat).abs() <= BigDecimal("0.01")) {
                declared.setScale(2, RoundingMode.HALF_UP)
            } else {
                computedVat
            }
        } ?: computedVat

        return LineAmounts(
            productName = position.productName ?: "Pozycja",
            quantity = quantity,
            unitPrice = unitPrice,
            net = net,
            vatRate = vatRate,
            vat = vat,
        )
    }

    private fun groupVatBuckets(lines: List<LineAmounts>): List<VatBucket> =
        lines.groupBy { it.vatRate }
            .map { (rate, items) ->
                VatBucket(
                    rate = rate,
                    net = items.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.net) },
                    vat = items.fold(BigDecimal.ZERO) { acc, line -> acc.add(line.vat) },
                )
            }
            .sortedBy { rateSortOrder(it.rate) }

    private fun rateSortOrder(rate: String): Int = when (rate) {
        "23" -> 1
        "22" -> 2
        "8" -> 3
        "7" -> 4
        "5" -> 5
        "4" -> 6
        "3" -> 7
        "0 KR", "0 WDT", "0 EX" -> 8
        "zw", "oo", "np I", "np II" -> 9
        else -> 99
    }

    private fun vatFromNet(net: BigDecimal, rateKey: String): BigDecimal {
        if (rateKey !in VAT_RATES_WITH_TAX) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        val percent = BigDecimal(rateKey)
        return net.multiply(percent).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }

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

    internal fun normalizeVatRate(vatRate: String?): String {
        val raw = vatRate?.trim()?.removeSuffix("%")?.trim() ?: "23"
        val lowered = raw.lowercase()
        when (lowered) {
            "0", "0.0" -> return "0 KR"
            "zw" -> return "zw"
            "oo" -> return "oo"
            "np i", "np 1" -> return "np I"
            "np ii", "np 2" -> return "np II"
        }

        val numeric = raw.replace(",", ".").toBigDecimalOrNull()
        if (numeric != null) {
            val percent = if (numeric < BigDecimal.ONE) {
                numeric.multiply(BigDecimal(100))
            } else {
                numeric
            }
            val plain = percent.stripTrailingZeros().toPlainString()
            return when (plain) {
                "0" -> "0 KR"
                else -> plain
            }
        }

        return when (raw) {
            "0 KR", "0 WDT", "0 EX", "np I", "np II" -> raw
            else -> throw KsefException("Unsupported VAT rate for KSeF P_12: $vatRate")
        }
    }

    private fun parseDecimal(value: String?, field: String): BigDecimal =
        value?.replace(",", ".")?.trim()?.toBigDecimalOrNull()
            ?: throw KsefException("Invalid decimal value for $field: $value")

    private fun formatAmount(value: BigDecimal): String =
        value.setScale(2, RoundingMode.HALF_UP).toPlainString()

    private fun formatDate(date: LocalDate): String =
        "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"

    private fun digitsOnly(nip: String?): String =
        nip?.filter { it.isDigit() }.orEmpty()

    private fun escapeXml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private data class LineAmounts(
        val productName: String,
        val quantity: BigDecimal,
        val unitPrice: BigDecimal,
        val net: BigDecimal,
        val vatRate: String,
        val vat: BigDecimal,
    )

    private data class VatBucket(
        val rate: String,
        val net: BigDecimal,
        val vat: BigDecimal,
    )
}
