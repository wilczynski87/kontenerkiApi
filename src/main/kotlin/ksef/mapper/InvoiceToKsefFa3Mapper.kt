package com.kontenery.ksef.mapper

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.mapper.fa3.Fa3InvoiceXmlWriter
import com.kontenery.ksef.mapper.fa3.Fa3VatCalculator
import com.kontenery.ksef.mapper.fa3.InvoiceToFa3DocumentMapper

/**
 * Facade: domain invoice → FA(3) XML.
 *
 * Layout and constants follow CIRFMF [ksef-client-java](https://github.com/CIRFMF/ksef-client-java)
 * sample `invoice-template_v3.xml` (document model + XML writer).
 */
object InvoiceToKsefFa3Mapper {

    fun toFa3Xml(invoice: Invoice): String {
        val document = InvoiceToFa3DocumentMapper.map(invoice)
        return Fa3InvoiceXmlWriter.write(document)
    }

    fun normalizeVatRate(vatRate: String?): String =
        Fa3VatCalculator.normalizeVatRate(vatRate)
}
