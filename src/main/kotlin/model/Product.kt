package com.kontenery.model

import com.kontenery.repository.entity.ProductType
import com.kontenery.utils.ByteArrayAsBase64Serializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import io.ktor.util.logging.*

@Serializable
sealed interface JoiningInterface

//@Serializable
//@Polymorphic
//@SerialName("PRODUCT")
//sealed class Product(
//    @Transient open val id: Long? = null,
//    @Transient open val name: String? = null,
//    @Transient open val location: String? = null,
//    @Transient open val type: ProductType? = ProductType.PRODUCT
//): Any() {
//    // TODO do wywalenia:
//    override fun toString(): String {
//        return "Product(id=$id, name=$name, location=$location, type=$type)"
//    }
//}

@Serializable
@Polymorphic
@SerialName("PRODUCT")
open class Product(
    @Transient open val id: Long? = null,
    @Transient open val name: String? = null,
    @Transient open val location: String? = null,
    @Transient open val type: ProductType? = ProductType.PRODUCT,
    @Transient open var client: Client? = null,
): Any(), JoiningInterface {
    // TODO do wywalenia:
    override fun toString(): String {
        return "Product(id=$id, name=$name, location=$location, type=$type, client=$client)"
    }
}

//@Serializable
//@Polymorphic
//@SerialName("PRODUCT")
//sealed class Product: JoiningInterface {
//    abstract val id: Long?
//    abstract val name: String?
//    abstract val location: String?
//    abstract val type: ProductType?
//}

//@Serializable
//@SerialName("CONTAINER")
//data class Container(
//    override val id: Long? = null,
//    override val name: String? = null,
//    override val location: String? = null,
////    override val type: ProductType? = ProductType.CONTAINER,
//    val length: String? = null,
//    val height: String? = null,
//    val color: String? = null,
//    val acquireDate: LocalDate? = null,
//    val lastPainting: LocalDate? = null,
//    val description: String? = null,
//    @Serializable(with = ByteArrayAsBase64Serializer::class) // Custom serializer for byte arrays
//    val photo: ByteArray? = null,
//): Product(), JoiningInterface {
//    val uom: String = "szt"
//    override val type: ProductType = ProductType.CONTAINER
//}

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
    override var client: Client? = null,
    val quantity: Long? = null
): Product(id, name, location, ProductType.YARD), JoiningInterface {
    val uom: String = "m2"
}

//@Serializable
//@SerialName("YARD")
//data class Yard(
//    override val id: Long? = null,
//    override val name: String? = null,
//    override val location: String? = null,
//    val quantity: Long? = null
//): Product(), JoiningInterface {
//    val uom: String = "m2"
//    override val type: ProductType = ProductType.YARD
//}

//val productModule = SerializersModule {
//    polymorphic(Product::class) {
//        subclass(Container::class, Container.serializer())
//        subclass(Yard::class, Yard.serializer())
//    }
//}
//
//val json = Json {
//    serializersModule = productModule
//    classDiscriminator = "type"
//    ignoreUnknownKeys = true
//}

//fun productDeserializer(jsonString: String): Product? =
//    try {
//        json.decodeFromString<Product>(jsonString)
//    } catch (e: Exception) {
//        null
//    }


fun productDeserializer(jsonString: String): Product? {

    return try {
        val json:Json = Json {
            ignoreUnknownKeys = true
        }
        try {
            return json.decodeFromString<Yard>(jsonString)
        } catch (_:Exception) {
        }

        try {
            return json.decodeFromString<Container>(jsonString)
        } catch (_:Exception) {
        }

        return json.decodeFromString<Product>(jsonString)

    } catch (e: Exception) {
        println(e)
        null
    }
}