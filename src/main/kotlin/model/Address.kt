package com.kontenery.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    var id:Long? = null,
    var street: String? = null,
    var house: String? = null,
    var city: String? = null,
    var postCode: String? = null,
    var country: String = "PL"
)
