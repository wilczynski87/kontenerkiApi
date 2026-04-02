package com.kontenery.data

import com.kontenery.data.serializers.ByteArrayAsBase64Serializer
import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.*

@Serializable
@Polymorphic
@SerialName("PRODUCT")
sealed class Product {
    abstract val id: Long?
    abstract val name: String?
    abstract val location: String?
    abstract var client: Client?

    @Serializable
    @SerialName("CONTAINER")
    data class Container(
        override val id: Long? = null,
        override val name: String? = null,
        override val location: String? = null,
        override var client: Client? = null,
        val length: String? = null,
        val height: String? = null,
        val color: String? = null,
        @Serializable(with = LocalDateSerializer::class)
        val acquireDate: LocalDate? = null,
        @Serializable(with = LocalDateSerializer::class)
        val lastPainting: LocalDate? = null,
        val description: String? = null,
        @Serializable(with = ByteArrayAsBase64Serializer::class) // Custom serializer for byte arrays
        val photo: ByteArray? = null,
    ): Product() {
        val uom: String = "szt"
    }

    @Serializable
    @SerialName("YARD")
    data class Yard(
        override val id: Long? = null,
        override val name: String? = null,
        override val location: String? = null,
        override var client: Client? = null,
        val quantity: Long? = null
    ): Product() {
        val uom: String = "m2"
    }

    companion object {
        fun createProductName(product: Product): String {
            val name = when(product) {
                is Container -> "Kontener ${product.length} - ${product.location}"
                is Yard -> "Plac ${product.quantity}m2"
            }
            return name
        }
    }
}

