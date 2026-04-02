package com.kontenery.data.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ByteArray::class)
object ByteArrayAsBase64Serializer: KSerializer<ByteArray> {

    override val descriptor = PrimitiveSerialDescriptor("ByteArray", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        val base64String = Base64.getEncoder().encodeToString(value) // Convert ByteArray to Base64
        encoder.encodeString(base64String)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return Base64.getDecoder().decode(decoder.decodeString()) // Convert Base64 back to ByteArray
    }

}