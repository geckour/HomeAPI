package com.geckour.homeapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class EnvironmentalData(
    val temperature: Float,
    val humidity: Float,
    val pressure: Float,
    val illuminance: Float
)