package com.kontenery.ksef.mapper.fa3

/**
 * FA(3) identifiers aligned with CIRFMF [ksef-client-java](https://github.com/CIRFMF/ksef-client-java)
 * (`SystemCode.FA_3`, sample `invoice-template_v3.xml`).
 */
object Fa3Constants {
    const val NAMESPACE = "http://crd.gov.pl/wzor/2025/06/25/13775/"
    const val NS_XSI = "http://www.w3.org/2001/XMLSchema-instance"
    const val NS_XSD = "http://www.w3.org/2001/XMLSchema"
    const val NS_ETD = "http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2022/01/05/eD/DefinicjeTypy/"

    const val SYSTEM_CODE = "FA (3)"
    const val SCHEMA_VERSION = "1-0E"
    const val FORM_CODE_VALUE = "FA"
    const val FORM_VARIANT = "3"

    const val CURRENCY_PLN = "PLN"
    const val INVOICE_TYPE_VAT = "VAT"
    const val SYSTEM_INFO = "Kontenerki"

    const val JST_NOT_APPLICABLE = "2"
    const val GV_NOT_APPLICABLE = "2"

    /** FA(3) [Zaplacono] — etd:TWybor1 (only "1" = paid in full). Omit when unpaid. */
    const val PAYMENT_PAID = "1"
    const val PAYMENT_FORM_TRANSFER = "6"

    val VAT_RATES_WITH_TAX = setOf("23", "22", "8", "7", "5", "4", "3")
    const val MAX_VAT_SUMMARY_SLOTS = 5
}
