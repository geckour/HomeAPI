package com.geckour.homeapi.model

import kotlinx.serialization.Serializable

@Serializable
data class OAuthToken(
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
    val refreshTokenExpiresAt: String,
    val client: Client,
    val user: User
) {

    @Serializable
    data class Client(
        val id: String
    )

    @Serializable
    data class User(
        val username: String
    )
}
