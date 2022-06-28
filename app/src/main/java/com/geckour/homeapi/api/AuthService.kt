package com.geckour.homeapi.api

import com.geckour.homeapi.model.OAuthToken
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    @POST("/oauth/token")
    @Headers("Authorization: Basic aG9tZUFQSUNsaWVudDpoTEJjblJpc2hPQm5pSFZ5N09UVHpiV1NxMkpKV2g=")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String
    ): OAuthToken

    @POST("/oauth/token")
    @Headers("Authorization: Basic aG9tZUFQSUNsaWVudDpoTEJjblJpc2hPQm5pSFZ5N09UVHpiV1NxMkpKV2g=")
    @FormUrlEncoded
    fun refreshAccessToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String
    ): Call<OAuthToken>
}