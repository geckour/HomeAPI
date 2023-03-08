package com.geckour.homeapi.api.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class EnvironmentalLog(
    val id: String,
    val temperature: Float,
    val humidity: Float,
    val pressure: Float,
    @Serializable(with = DateSerializer::class)
    val date: Date
)

class DateSerializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(Date::class.java.name, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date =
        checkNotNull(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.JAPAN).parse(decoder.decodeString()))

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.toString())
    }
}