package com.kontenery.model

import com.kontenery.utils.BigDecimalSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Client(
    val id: Long? = null,
    val clientPrivate: ClientPersonalData? = null,
    val clientCompany: ClientCompanyData? = null,
    val isActive: Boolean? = null,
    val createdAt: LocalDate? = null,
    val updatedAt: LocalDate? = null,
    val invoiceTitle: String? = null,
) {
    fun getName():String {
        return clientCompany?.name
            ?: "${clientPrivate?.firstName} ${clientPrivate?.lastName}".ifBlank { "No Name" }
    }

    fun needInvoice():Boolean {
        return clientCompany?.needInvoice == null || clientCompany.needInvoice == false
    }
}

@Serializable
data class ClientPersonalData(
    val id: Long? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var pesel: String? = null,
    var passport: String? = null,
    var address: Address? = null,
    var phone: String? = null,
    var email: String? = null,
    var salutation: String = "Drogi Kliencie"
)

@Serializable
data class ClientCompanyData(
    val id: Long? = null,
    val name: String? = null,
    val nip: String? = null,
    val krs: String? = null,
    val address: Address? = null,
    val phone: String? = null,
    val email: String? = null,
    val needInvoice: Boolean? = null,
)

@Serializable
data class ClientOnList(
    val id: Long,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val paymentsOverdue: BigDecimal? = BigDecimal.ZERO,
    val contracts: String?,
    val active: Boolean,
    val invoice: Boolean,
    val lastBill: LocalDate?
) {
}