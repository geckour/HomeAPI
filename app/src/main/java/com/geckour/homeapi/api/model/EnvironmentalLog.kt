package com.geckour.homeapi.api.model

import com.geckour.homeapi.api.DateSerializer
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