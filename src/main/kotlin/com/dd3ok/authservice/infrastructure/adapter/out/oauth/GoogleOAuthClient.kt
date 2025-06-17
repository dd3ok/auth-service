package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import com.dd3ok.authservice.application.port.out.OAuthTokenResponse
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate


@Component
class GoogleOAuthClient(
    @Value("\${oauth.google.client-id}") private val clientId: String,
    @Value("\${oauth.google.client-secret}") private val clientSecret: String,
    @Value("\${oauth.google.redirect-uri}") private val redirectUri: String,
    private val restTemplate: RestTemplate
) {
    
    companion object {
        private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
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
            add("redirect_uri", redirectUri ?: this@GoogleOAuthClient.redirectUri)
        }
        
        val request = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(TOKEN_URL, request, GoogleTokenResponse::class.java)
        
        val tokenResponse = response.body 
            ?: throw RuntimeException("Failed to get token from Google")
        
        return OAuthTokenResponse(
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            tokenType = tokenResponse.tokenType ?: "Bearer",
            expiresIn = tokenResponse.expiresIn ?: 3600
        )
    }
    
    fun getUserInfo(accessToken: String): OAuthUserInfoResponse {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }
        
        val request = HttpEntity(null, headers)
        val response = restTemplate.exchange(
            USER_INFO_URL,
            HttpMethod.GET,
            request,
            GoogleUserInfoResponse::class.java
        )
        
        val userInfo = response.body
            ?: throw RuntimeException("Failed to get user info from Google")
        
        return OAuthUserInfoResponse(
            id = userInfo.id,
            email = userInfo.email,
            name = userInfo.name,
            profileImageUrl = userInfo.picture
        )
    }
}

data class GoogleTokenResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String?,
    @JsonProperty("token_type") val tokenType: String?,
    @JsonProperty("expires_in") val expiresIn: Long?
)

data class GoogleUserInfoResponse(
    val id: String,
    val email: String,
    val name: String,
    val picture: String?
)
