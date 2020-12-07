package com.geckour.homeapi.api.model

import kotlinx.serialization.Serializable

@Serializable
data class Data<T>(
    val data: T
)
