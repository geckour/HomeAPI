package com.geckour.homeapi.model

data class RequestData(
    val emoji: String,
    val name: String,
    val onClick: () -> Unit
)