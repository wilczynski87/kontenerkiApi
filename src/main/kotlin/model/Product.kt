package com.kontenery.model

import com.kontenery.repository.entity.ProductType
import com.kontenery.utils.ByteArrayAsBase64Serializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface JoiningInterface

@Serializable
@Polymorphic
@SerialName("PRODUCT")
sealed class Product(
    @Transient open val id: Long? = null,
//    open val id: Long? = null,
    @Transient open val name: String? = null,
    @Transient open val location: String? = null,
    @Transient open val type: ProductType? = ProductType.PRODUCT
): JoiningInterface {
    // TODO do wywalenia:
    override fun toString(): String {
        return "Product(id=$id, name=$name, location=$location, type=$type)"
    }
}

@Serializable
@SerialName("CONTAINER")
data class Container(
    override val id: Long? = null,
    override val name: String? = null,
    override val location: String? = null,
    val length: String? = null,
    val height: String? = null,
    val color: String? = null,
    val acquireDate: LocalDate? = null,
    val lastPainting: LocalDate? = null,
    val description: String? = null,
    @Serializable(with = ByteArrayAsBase64Serializer::class) // Custom serializer for byte arrays
    val photo: ByteArray? = null,
): Product(id, name, location, ProductType.CONTAINER), JoiningInterface {
    val uom: String = "szt"
}

@Serializable
@SerialName("YARD")
data class Yard(
    override val id: Long? = null,
    override val name: String? = null,
    override val location: String? = null,
    val quantity: Long? = null
): Product(id, name, location, ProductType.YARD), JoiningInterface {
    val uom: String = "m2"
}

@Serializable
@SerialName("GENERAL_PRODUCT")
data class GeneralProduct(
    override val id: Long? = null,
): Product(id, null, null, ProductType.PRODUCT), JoiningInterface {
}

//fun productDeserializer(json:String):Product? {
//    return try {
//        // container
//        try {
//            return Json.decodeFromString<Container>(json)
//        }  catch (e: SerializationException) {
//            null
//        }
//
//        // yard
//        try {
//            return Json.decodeFromString<Yard>(json)
//        } catch (e: SerializationException) {
//            null
//        }
//
//        // product
//        try {
//            Json.decodeFromString<Product>(json)
//        } catch (e: SerializationException) {
//            null
//        }
//
//    } catch (e: Exception) {
//        null
//    }
//}
val productModule = SerializersModule {
    polymorphic(Product::class) {
        subclass(Container::class, Container.serializer())
        subclass(Yard::class, Yard.serializer())
        subclass(GeneralProduct::class, GeneralProduct.serializer())
    }
}

val json = Json {
    serializersModule = productModule
    classDiscriminator = "type"
    ignoreUnknownKeys = true
}

fun productDeserializer(jsonString: String): Product? =
    try {
        json.decodeFromString<Product>(jsonString)
    } catch (e: Exception) {
        null
    }