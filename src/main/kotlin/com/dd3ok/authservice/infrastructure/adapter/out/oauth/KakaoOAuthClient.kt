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
class KakaoOAuthClient(
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.client-secret}") private val clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri}") private val redirectUri: String,
    private val restTemplate: RestTemplate
) {
    companion object {
        private const val TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
    }

    fun getAccessToken(authorizationCode: String, redirectUri: String?): OAuthTokenResponse {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
        val body = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("code", authorizationCode)
            add("redirect_uri", redirectUri ?: this@KakaoOAuthClient.redirectUri)
        }
        val request = HttpEntity(body, headers)
        try {
        val response = restTemplate.postForEntity(TOKEN_URL, request, KakaoTokenResponse::class.java)
        
        val tokenResponse = response.body ?: throw RuntimeException("Failed to get token from Kakao")
        
        return OAuthTokenResponse(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            tokenType = tokenResponse.tokenType,
            expiresIn = tokenResponse.expiresIn
        )
        } catch (e: Exception) {
            throw RuntimeException("카카오 토큰 요청 실패", e)
        }
    }

    fun getUserInfo(accessToken: String): OAuthUserInfoResponse {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }
        val request = HttpEntity(null, headers)
        val response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, request, KakaoUserInfoResponse::class.java)

        val userInfo = response.body ?: throw RuntimeException("Failed to get user info from Kakao")

        return OAuthUserInfoResponse(
            id = userInfo.id.toString(),
            email = userInfo.kakaoAccount.email,
            name = userInfo.kakaoAccount.profile.nickname,
            profileImageUrl = userInfo.kakaoAccount.profile.profileImageUrl
        )
    }
}

// Kakao 응답 DTO들
data class KakaoTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long,
)

data class KakaoUserInfoResponse(
    val id: Long,
    @JsonProperty("kakao_account") val kakaoAccount: KakaoAccount
) {
    data class KakaoAccount(
        val email: String,
        val profile: Profile
    ) {
        data class Profile(
            val nickname: String,
            @JsonProperty("profile_image_url") val profileImageUrl: String?
        )
    }
}
