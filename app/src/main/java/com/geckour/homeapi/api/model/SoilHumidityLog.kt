package com.geckour.homeapi.api.model

import com.geckour.homeapi.api.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SoilHumidityLog(
    val id: String,
    val value: Float,
    @SerialName("raw_value")
    val rawValue: Int,
    @Serializable(with = DateSerializer::class)
    val date: Date
)