package com.kontenery.model

data class Client(
    val id: Long? = null,
    val clientPrivate: ClientPersonalData? = null,
    val clientCompany: ClientCompanyData? = null,
    val isActive: Boolean? = null,
)

data class ClientPersonalData(
    val id: Long? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var pesel: String? = null,
    var passport: String? = null,
    var address: Address? = null,
    var phone: String? = null,
    var email: String? = null,
)

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