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

    fun toFa3Xml(invoice: Invoice): String {
        validate(invoice)
        val seller = invoice.seller!!
        val customer = invoice.customer!!
        val invoiceNumber = invoice.invoiceNumber!!.trim()
        val issueDate = invoice.invoiceDate!!
        val saleDate = invoice.paymentDay ?: issueDate
        val netTotal = sumPositions(invoice.products) { it.price }
        val vatTotal = sumPositions(invoice.products) { it.vatAmount }
        val grossTotal = sumPositions(invoice.products) { it.priceWithVat }
            .takeIf { it > BigDecimal.ZERO }
            ?: netTotal.add(vatTotal)

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
            invoice.products.forEachIndexed { index, position ->
                appendFaWiersz(index + 1, position)
            }
            appendLine("        <P_13_1>${formatAmount(netTotal)}</P_13_1>")
            appendLine("        <P_14_1>${formatAmount(vatTotal)}</P_14_1>")
            appendLine("        <P_15>${formatAmount(grossTotal)}</P_15>")
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
        require(!invoice.seller!!.nip.isNullOrBlank()) { "seller NIP is required for KSeF" }
        require(!invoice.customer!!.nip.isNullOrBlank()) { "customer NIP is required for KSeF" }
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
        phone?.trim()?.takeIf { it.isNotEmpty() }?.take(16)?.let {
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

    private fun StringBuilder.appendFaWiersz(rowNumber: Int, position: Position) {
        val quantity = parseDecimal(position.quantity, "quantity")
        val unitPrice = parseDecimal(position.unitPrice, "unitPrice")
        val netAmount = parseDecimal(position.price, "price")
        val vatRate = normalizeVatRate(position.vatRate)
        appendLine("        <FaWiersz>")
        appendLine("            <NrWierszaFa>$rowNumber</NrWierszaFa>")
        appendLine("            <UU_ID>${UUID.randomUUID()}</UU_ID>")
        appendLine("            <P_7>${escapeXml(position.productName ?: "Pozycja $rowNumber")}</P_7>")
        appendLine("            <P_8A>szt.</P_8A>")
        appendLine("            <P_8B>${formatAmount(quantity)}</P_8B>")
        appendLine("            <P_9A>${formatAmount(unitPrice)}</P_9A>")
        appendLine("            <P_11>${formatAmount(netAmount)}</P_11>")
        appendLine("            <P_12>${escapeXml(vatRate)}</P_12>")
        appendLine("        </FaWiersz>")
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

    private fun normalizeVatRate(vatRate: String?): String {
        val raw = vatRate?.trim()?.removeSuffix("%")?.trim() ?: "23"
        return when (raw.lowercase()) {
            "0", "0.0" -> "0 KR"
            "zw" -> "zw"
            else -> raw
        }
    }

    private fun sumPositions(products: List<Position>, selector: (Position) -> String?): BigDecimal =
        products.fold(BigDecimal.ZERO) { acc, position ->
            acc.add(parseDecimal(selector(position), "position amount"))
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
}
