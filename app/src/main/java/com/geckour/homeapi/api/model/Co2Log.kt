package com.geckour.homeapi.api.model

import com.geckour.homeapi.api.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Co2Log(
    val id: String,
    val value: Int,
    @Serializable(with = DateSerializer::class)
    val date: Date
)