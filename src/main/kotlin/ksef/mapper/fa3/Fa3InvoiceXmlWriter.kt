package com.kontenery.ksef.mapper.fa3

/**
 * Serializes [Fa3InvoiceDocument] to FA(3) XML.
 * Element order matches CIRFMF `invoice-template_v3.xml` and official XSD (summaries before lines).
 */
object Fa3InvoiceXmlWriter {

    fun write(document: Fa3InvoiceDocument): String = buildString {
        appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        appendLine(
            """<Faktura xmlns:xsi="${Fa3Constants.NS_XSI}" """ +
                """xmlns:xsd="${Fa3Constants.NS_XSD}" """ +
                """xmlns:etd="${Fa3Constants.NS_ETD}" """ +
                """xmlns="${Fa3Constants.NAMESPACE}">""",
        )
        appendHeader(document.header)
        appendParty("Podmiot1", document.seller)
        appendBuyer(document.buyer)
        appendFa(document.body)
        appendLine("</Faktura>")
    }

    private fun StringBuilder.appendHeader(header: Fa3Header) {
        appendLine("    <Naglowek>")
        appendLine(
            """        <KodFormularza kodSystemowy="${Fa3Constants.SYSTEM_CODE}" """ +
                """wersjaSchemy="${Fa3Constants.SCHEMA_VERSION}">${Fa3Constants.FORM_CODE_VALUE}</KodFormularza>""",
        )
        appendLine("        <WariantFormularza>${Fa3Constants.FORM_VARIANT}</WariantFormularza>")
        appendLine("        <DataWytworzeniaFa>${header.productionDateTime}</DataWytworzeniaFa>")
        appendLine("        <SystemInfo>${Fa3Constants.SYSTEM_INFO}</SystemInfo>")
        appendLine("    </Naglowek>")
    }

    private fun StringBuilder.appendParty(tag: String, party: Fa3Party) {
        appendLine("    <$tag>")
        appendLine("        <DaneIdentyfikacyjne>")
        appendLine("            <NIP>${party.nip}</NIP>")
        appendLine("            <Nazwa>${escapeXml(party.name)}</Nazwa>")
        appendLine("        </DaneIdentyfikacyjne>")
        appendAddress(party.address)
        appendContact(party.email, party.phoneDigits)
        appendLine("    </$tag>")
    }

    private fun StringBuilder.appendBuyer(buyer: Fa3Buyer) {
        appendLine("    <Podmiot2>")
        appendLine("        <DaneIdentyfikacyjne>")
        appendLine("            <NIP>${buyer.nip}</NIP>")
        appendLine("            <Nazwa>${escapeXml(buyer.name)}</Nazwa>")
        appendLine("        </DaneIdentyfikacyjne>")
        appendAddress(buyer.address)
        appendContact(buyer.email, buyer.phoneDigits)
        buyer.clientNumber?.let {
            appendLine("        <NrKlienta>${escapeXml(it)}</NrKlienta>")
        }
        appendLine("        <JST>${buyer.jst}</JST>")
        appendLine("        <GV>${buyer.gv}</GV>")
        appendLine("    </Podmiot2>")
    }

    private fun StringBuilder.appendAddress(address: Fa3Address) {
        appendLine("        <Adres>")
        appendLine("            <KodKraju>${escapeXml(address.countryCode)}</KodKraju>")
        appendLine("            <AdresL1>${escapeXml(address.line1)}</AdresL1>")
        appendLine("            <AdresL2>${escapeXml(address.line2)}</AdresL2>")
        appendLine("        </Adres>")
    }

    private fun StringBuilder.appendContact(email: String, phoneDigits: String?) {
        appendLine("        <DaneKontaktowe>")
        appendLine("            <Email>${escapeXml(email)}</Email>")
        phoneDigits?.let {
            appendLine("            <Telefon>${escapeXml(it)}</Telefon>")
        }
        appendLine("        </DaneKontaktowe>")
    }

    private fun StringBuilder.appendFa(body: Fa3Body) {
        appendLine("    <Fa>")
        appendLine("        <KodWaluty>${Fa3Constants.CURRENCY_PLN}</KodWaluty>")
        appendLine("        <P_1>${formatDate(body.issueDate)}</P_1>")
        appendLine("        <P_2>${escapeXml(body.invoiceNumber)}</P_2>")
        appendLine("        <P_6>${formatDate(body.saleDate)}</P_6>")
        body.vatSummaries.forEach { slot ->
            appendLine("        <P_13_${slot.slotIndex}>${slot.netAmount}</P_13_${slot.slotIndex}>")
            slot.vatAmount?.let { vat ->
                appendLine("        <P_14_${slot.slotIndex}>$vat</P_14_${slot.slotIndex}>")
            }
        }
        appendLine("        <P_15>${body.grossTotal}</P_15>")
        appendStandardAnnotations()
        appendLine("        <RodzajFaktury>${Fa3Constants.INVOICE_TYPE_VAT}</RodzajFaktury>")
        body.lines.forEach { appendFaWiersz(it) }
        appendPayment(body.payment)
        appendLine("    </Fa>")
    }

    private fun StringBuilder.appendFaWiersz(line: Fa3Line) {
        appendLine("        <FaWiersz>")
        appendLine("            <NrWierszaFa>${line.rowNumber}</NrWierszaFa>")
        appendLine("            <UU_ID>${line.lineUuid}</UU_ID>")
        appendLine("            <P_7>${escapeXml(line.productName)}</P_7>")
        appendLine("            <P_8A>${escapeXml(line.unit)}</P_8A>")
        appendLine("            <P_8B>${line.quantity}</P_8B>")
        appendLine("            <P_9A>${line.unitPriceNet}</P_9A>")
        appendLine("            <P_11>${line.netAmount}</P_11>")
        appendLine("            <P_12>${escapeXml(line.vatRate)}</P_12>")
        appendLine("        </FaWiersz>")
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

    private fun StringBuilder.appendPayment(payment: Fa3Payment) {
        appendLine("        <Platnosc>")
        if (payment.isPaid) {
            appendLine("            <Zaplacono>${Fa3Constants.PAYMENT_PAID}</Zaplacono>")
            val paidOn = payment.paymentDate ?: payment.dueDate
            appendLine("            <DataZaplaty>${formatDate(paidOn)}</DataZaplaty>")
        } else {
            appendLine("            <TerminPlatnosci>")
            appendLine("                <Termin>${formatDate(payment.dueDate)}</Termin>")
            appendLine("            </TerminPlatnosci>")
        }
        appendLine("            <FormaPlatnosci>${payment.paymentForm}</FormaPlatnosci>")
        payment.bankAccountDigits?.let { digits ->
            appendLine("            <RachunekBankowy>")
            appendLine("                <NrRB>$digits</NrRB>")
            appendLine("            </RachunekBankowy>")
        }
        appendLine("        </Platnosc>")
    }

    private fun formatDate(date: kotlinx.datetime.LocalDate): String =
        "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"

    private fun escapeXml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
