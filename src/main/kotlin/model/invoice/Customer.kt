package com.kontenery.model.invoice

import com.kontenery.model.Address

sealed class Subject {
    abstract val name:String
    abstract val address: Address
    abstract val nip:String
    abstract val email:String
    abstract val phone:String?
    abstract val invoiceId:Long

    data class Customer(
        override val name:String,
        override val address: Address,
        override val nip:String,
        override val email:String,
        override val phone:String? = null,
        override val invoiceId:Long,
        val salutation:String = "Drogi Kliencie",
    ): Subject()

    data class Seller(
        override val name:String = "Karol Wilczyński",
        override val address: Address = Address(null, "Aleksandra Ostrowskiego", "102", "Wrocław", "53-238", "PL"),
        override val nip:String = "8942957044",
        override val email:String = "parkingostrowskiego@gmail.com",
        override val phone:String? = "+48 507 036 484",
        override val invoiceId:Long,
        val account: String = "50 1950 0001 2006 0023 6241 0001"
    ): Subject() {
        fun personal(invoiceId: Long): Seller = Seller(
            "Karol Wilczyński",
            Address(null, "Aleksandra Ostrowskiego", "102", "Wrocław", "53-238", "PL"),
            "",
            "wilczynski87@gmail.com",
            "+48 507 036 484",
            invoiceId,
            "11 2490 1044 0000 4200 8845 2192"
        )
    }
}
