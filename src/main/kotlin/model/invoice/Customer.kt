package com.kontenery.model.invoice

import com.kontenery.model.Address
import com.kontenery.model.Client
import com.kontenery.repository.entity.invoice.SubjectType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

const val sellerName:String = "SELLER"
const val customerName:String = "CUSTOMER"

@Serializable
@Polymorphic
sealed class Subject {
    abstract val name:String
    abstract val address: Address
    abstract val nip:String
    abstract val email:String
    abstract val phone:String?
    abstract var invoiceNumber:String?

    @Serializable
    data class Customer(
        override val name:String,
        override val address: Address,
        override val nip:String,
        override val email:String,
        override val phone:String? = null,
        override var invoiceNumber:String? = null,
        val salutation:String = "Drogi Kliencie",
    ): Subject() {
        companion object {
            fun toCustomer(client: Client, invoiceNumber: String? = null): Customer {
                return Customer(
                    name = client.getName(),
                    address = client.clientCompany?.address ?: client.clientPrivate?.address ?: Address(),
                    nip = client.clientCompany?.nip ?: "",
                    email = client.clientCompany?.email ?: client.clientPrivate?.email ?: "",
                    phone = client.clientCompany?.phone ?: client.clientPrivate?.phone ?: "",
                    invoiceNumber = invoiceNumber,
                    salutation = client.clientPrivate?.salutation ?: "Drogi Kliencie"
                )
            }
        }
    }

    @Serializable
    data class Seller(
        override val name:String = "Karol Wilczyński",
        override val address: Address = Address(null, "Aleksandra Ostrowskiego", "102", "Wrocław", "53-238", "PL"),
        override val nip:String = "8942957044",
        override val email:String = "parkingostrowskiego@gmail.com",
        override val phone:String? = "+48 507 036 484",
        override var invoiceNumber:String?,
        val account: String = "50 1950 0001 2006 0023 6241 0001"
    ): Subject() {
        fun personal(invoiceId: Long): Seller = Seller(
            "Karol Wilczyński",
            Address(null, "Aleksandra Ostrowskiego", "102", "Wrocław", "53-238", "PL"),
            "",
            "wilczynski87@gmail.com",
            "+48 507 036 484",
            invoiceNumber,
            "11 2490 1044 0000 4200 8845 2192"
        )
    }
}
