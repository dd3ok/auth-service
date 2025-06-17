package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import com.dd3ok.authservice.application.port.out.OAuthTokenResponse
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Component
class NaverOAuthClient(
    @Value("\${oauth.naver.client-id}") private val clientId: String,
    @Value("\${oauth.naver.client-secret}") private val clientSecret: String,
    @Value("\${oauth.naver.redirect-uri}") private val redirectUri: String,
    private val restTemplate: RestTemplate
) {
    companion object {
        private const val TOKEN_URL = "https://nid.naver.com/oauth2.0/token"
        private const val USER_INFO_URL = "https://openapi.naver.com/v1/nid/me"
    }

    fun getAccessToken(authorizationCode: String, state: String, redirectUri: String?): OAuthTokenResponse {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code", authorizationCode)
            add("state", state)
            add("redirect_uri", redirectUri ?: this@NaverOAuthClient.redirectUri)
        }
        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(TOKEN_URL, request, NaverTokenResponse::class.java)

        val tokenResponse = response.body ?: throw RuntimeException("Failed to get token from Naver")

        return OAuthTokenResponse(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            tokenType = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn.toLong()
        )
    }

    fun getUserInfo(accessToken: String): OAuthUserInfoResponse {
        val headers = HttpHeaders().apply { setBearerAuth(accessToken) }
        val request = HttpEntity(null, headers)
        val response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, NaverUserInfoResponse::class.java)

        val userInfo = response.body?.response ?: throw RuntimeException("Failed to get user info from Naver")

        return OAuthUserInfoResponse(
            id = userInfo.id,
            email = userInfo.email,
            name = userInfo.name,
            profileImageUrl = userInfo.profileImage
        )
    }
}

// Naver 응답 DTO들
data class NaverTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Int,
)

data class NaverUserInfoResponse(
    val response: NaverUser
) {
    data class NaverUser(
        val id: String,
        val email: String,
        val name: String,
        @JsonProperty("profile_image") val profileImage: String?
    )
}
