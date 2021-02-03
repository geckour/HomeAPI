package com.geckour.homeapi.model

data class RequestData(
    val name: String,
    val onClick: () -> Unit
)