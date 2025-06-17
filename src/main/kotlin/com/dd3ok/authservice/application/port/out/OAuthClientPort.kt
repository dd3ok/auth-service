package com.dd3ok.authservice.application.port.out

import com.dd3ok.authservice.domain.vo.OAuthProvider

interface OAuthClientPort {
    fun getAccessToken(provider: OAuthProvider, authorizationCode: String, redirectUri: String?, state: String?): OAuthTokenResponse
    fun getUserInfo(provider: OAuthProvider, accessToken: String): OAuthUserInfoResponse
}

data class OAuthTokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long
)

data class OAuthUserInfoResponse(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?
)
