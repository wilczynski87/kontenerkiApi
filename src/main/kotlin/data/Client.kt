package com.kontenery.data

import com.kontenery.data.serializers.BigDecimalSerializer
import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import kotlinx.datetime.LocalDate

@Serializable
data class Client(
    val id: Long? = null,
    val clientPrivate: ClientPersonalData? = null,
    val clientCompany: ClientCompanyData? = null,
    val isActive: Boolean? = null,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val updatedAt: LocalDate? = null,
    val invoiceTitle: String? = null,
    val bankAccounts: List<String>? = null
) {
    fun getName():String {
        return clientCompany?.name
            ?: "${clientPrivate?.firstName} ${clientPrivate?.lastName}".ifBlank { "No Name" }
    }

    fun needInvoice():Boolean {
        return clientCompany?.needInvoice == true
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
    val contracts: List<String>?,
    val active: Boolean,
    val invoice: Boolean,
    val lastBill: LocalDate?,
    val clientType: ClientType = ClientType.UNKNOWN  // ENUM for individual/company
) {
    enum class ClientType { INDIVIDUAL, COMPANY, UNKNOWN }
}

@Serializable
data class ClientBankAccount(
    val id: Long? = null,
    val bankAccount: String? = null,
    val client: Client? = null,
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDate? = null,
)

@Serializable
data class ClientOnListForFinance(
    val clientId: Long? = null,
    val name: String? = null,
    val isActive: Boolean? = null,
)