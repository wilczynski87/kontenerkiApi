package com.kontenery.model

import com.kontenery.repository.entity.ProductType
import com.kontenery.utils.ByteArrayAsBase64Serializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import io.ktor.util.logging.*

//@Serializable
//sealed interface JoiningInterface

//@Serializable
//@Polymorphic
//@SerialName("PRODUCT")
//open class Product(
//    @Transient open val id: Long? = null,
//    @Transient open val name: String? = null,
//    @Transient open val location: String? = null,
//    @Transient open val type: ProductType? = ProductType.PRODUCT,
//    @Transient open var client: Client? = null,
//): Any(), JoiningInterface {
//    // TODO do wywalenia:
//    override fun toString(): String {
//        return "Product(id=$id, name=$name, location=$location, type=$type, client=$client)"
//    }
//}
//
//@Serializable
//@SerialName("CONTAINER")
//data class Container(
//    override val id: Long? = null,
//    override val name: String? = null,
//    override val location: String? = null,
//    override var client: Client? = null,
//    val length: String? = null,
//    val height: String? = null,
//    val color: String? = null,
//    val acquireDate: LocalDate? = null,
//    val lastPainting: LocalDate? = null,
//    val description: String? = null,
//    @Serializable(with = ByteArrayAsBase64Serializer::class) // Custom serializer for byte arrays
//    val photo: ByteArray? = null,
//): Product(id, name, location, ProductType.CONTAINER), JoiningInterface {
//    val uom: String = "szt"
//}
//
//@Serializable
//@SerialName("YARD")
//data class Yard(
//    override val id: Long? = null,
//    override val name: String? = null,
//    override val location: String? = null,
//    override var client: Client? = null,
//    val quantity: Long? = null
//): Product(id, name, location, ProductType.YARD), JoiningInterface {
//    val uom: String = "m2"
//}


@Serializable
@Polymorphic
@SerialName("PRODUCT")
sealed class Product {
    abstract val id: Long?
    abstract val name: String?
    abstract val location: String?
//    abstract val type: ProductType?
    abstract var client: Client?

    @Serializable
    @SerialName("CONTAINER")
    data class Container(
        override val id: Long? = null,
        override val name: String? = null,
        override val location: String? = null,
//        override val type: ProductType? = ProductType.CONTAINER,
        override var client: Client? = null,
        val length: String? = null,
        val height: String? = null,
        val color: String? = null,
        val acquireDate: LocalDate? = null,
        val lastPainting: LocalDate? = null,
        val description: String? = null,
        @Serializable(with = ByteArrayAsBase64Serializer::class) // Custom serializer for byte arrays
        val photo: ByteArray? = null,
    ): Product() {
        val uom: String = "m2"
    }

    @Serializable
    @SerialName("YARD")
    data class Yard(
        override val id: Long? = null,
        override val name: String? = null,
        override val location: String? = null,
//        override val type: ProductType? = ProductType.CONTAINER,
        override var client: Client? = null,
        val quantity: Long? = null
    ): Product() {
        val uom: String = "m2"
    }

}