package com.kontenery

import com.kontenery.library.model.Product
import com.kontenery.library.model.invoice.Subject
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun Application.configureSerialization() {

    val productModule = SerializersModule {
        polymorphic(Product::class) {
            subclass(Product.Container::class, Product.Container.serializer())
            subclass(Product.Yard::class, Product.Yard.serializer())
        }
        polymorphic(Subject::class) {
            subclass(Subject.Seller::class, Subject.Seller.serializer())
            subclass(Subject.Customer::class, Subject.Customer.serializer())
        }
    }

    val json = Json {
        serializersModule = productModule
        classDiscriminator = "type"
        ignoreUnknownKeys = true
    }


    install(ContentNegotiation) {
        json(json)
    }
}
