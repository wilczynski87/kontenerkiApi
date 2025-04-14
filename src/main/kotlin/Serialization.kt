package com.kontenery

import com.kontenery.model.Product
import com.kontenery.model.Yard
import com.kontenery.model.Container
import com.kontenery.model.GeneralProduct
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun Application.configureSerialization() {

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


    install(ContentNegotiation) {
//        json()
        json(json)
//        json(Json {
//            prettyPrint = true
//            isLenient = true
//            encodeDefaults = true
//            classDiscriminator = "type" // <--- ważne!
//        })
    }
}
