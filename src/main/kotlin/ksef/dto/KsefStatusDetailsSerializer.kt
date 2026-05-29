package com.kontenery.ksef.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * KSeF API returns [details] as a string or as a JSON array of validation messages (FA(3) semantic errors).
 */
object KsefStatusDetailsSerializer : KSerializer<String?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("KsefStatusDetails", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return decoder.decodeString().takeIf { it.isNotBlank() }
        return flattenDetails(jsonDecoder.decodeJsonElement())
    }

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }

    private fun flattenDetails(element: JsonElement): String? = when (element) {
        is JsonNull -> null
        is JsonPrimitive -> element.primitiveContentOrNull()
        is JsonArray -> element.mapNotNull { flattenDetails(it) }
            .filter { it.isNotBlank() }
            .joinToString("; ")
            .takeIf { it.isNotBlank() }
        is JsonObject -> element.entries
            .mapNotNull { (_, value) -> flattenDetails(value) }
            .filter { it.isNotBlank() }
            .joinToString("; ")
            .takeIf { it.isNotBlank() }
            ?: element["message"]?.jsonPrimitive?.primitiveContentOrNull()
            ?: element["description"]?.jsonPrimitive?.primitiveContentOrNull()
        else -> null
    }

    private fun JsonPrimitive.primitiveContentOrNull(): String? =
        if (isString) content.trim().takeIf { it.isNotEmpty() } else null
}
