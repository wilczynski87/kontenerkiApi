package com.kontenery.data.utils

enum class Path(val path: String) {
    PERIODIC_INVOICE_PDF("invoices/fakturaKontenery"),
    UTILITIES_INVOICE_PDF("invoices/fakturaMedia"),
    PERIODIC_MAIL("mails/invoicePeriodic"),
    UTILITIES_MAIL("mails/invoiceUtilities"),
    PRINT_MAIL("mails/printInvoices"),
    REMAINDER_MAIL("mails/remainder"),
    ANNUAL_REVALUATION_MAIL("mails/annualRevaluation"),
    FONT("templates/thymeleaf/fonts")
}