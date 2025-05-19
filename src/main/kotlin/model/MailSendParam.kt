package model

enum class MailSendParam(val param:String) {
    INVOICE_NUMBER("invoiceNumber"),
    SEND_DATE("sendDate"),
    ERROR("error"),
    MESSAGE("message"),
}