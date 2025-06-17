package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@DisplayName("GoogleOAuthClient 테스트")
class GoogleOAuthClientTest {

    private val restTemplate = mock<RestTemplate>()
    private lateinit var googleOAuthClient: GoogleOAuthClient

    @BeforeEach
    fun setUp() {
        googleOAuthClient = GoogleOAuthClient(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            redirectUri = "http://localhost:8080/callback",
            restTemplate = restTemplate
        )
    }

    @Test
    @DisplayName("Authorization Code로 Access Token을 요청할 수 있다")
    fun shouldGetAccessTokenWithAuthorizationCode() {
        // Given
        val authCode = "test-auth-code"
        val redirectUri = "http://localhost:8080/callback"

        val tokenResponse = GoogleTokenResponse(
            accessToken = "google-access-token",
            refreshToken = "google-refresh-token",
            tokenType = "Bearer",
            expiresIn = 3600L
        )

        val responseEntity = ResponseEntity.ok(tokenResponse)
        doReturn(responseEntity).whenever(restTemplate)
            .postForEntity(any<String>(), any<HttpEntity<*>>(), eq(GoogleTokenResponse::class.java))

        // When
        val result = googleOAuthClient.getAccessToken(authCode, redirectUri)

        // Then
        assertEquals("google-access-token", result.accessToken)
        assertEquals("google-refresh-token", result.refreshToken)
        assertEquals("Bearer", result.tokenType)
        assertEquals(3600L, result.expiresIn)

        verify(restTemplate).postForEntity(
            eq("https://oauth2.googleapis.com/token"),
            any<HttpEntity<*>>(),
            eq(GoogleTokenResponse::class.java)
        )
    }

    @Test
    @DisplayName("Access Token으로 사용자 정보를 요청할 수 있다")
    fun shouldGetUserInfoWithAccessToken() {
        // Given
        val accessToken = "valid-access-token"

        val userInfoResponse = GoogleUserInfoResponse(
            id = "google-user-123",
            email = "user@gmail.com",
            name = "Google User",
            picture = "https://lh3.googleusercontent.com/photo.jpg"
        )

        val responseEntity = ResponseEntity.ok(userInfoResponse)
        doReturn(responseEntity).whenever(restTemplate)
            .exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(GoogleUserInfoResponse::class.java)
            )

        // When
        val result = googleOAuthClient.getUserInfo(accessToken)

        // Then
        assertEquals("google-user-123", result.id)
        assertEquals("user@gmail.com", result.email)
        assertEquals("Google User", result.name)
        assertEquals("https://lh3.googleusercontent.com/photo.jpg", result.profileImageUrl)

        verify(restTemplate).exchange(
            eq("https://www.googleapis.com/oauth2/v2/userinfo"),
            eq(HttpMethod.GET),
            any<HttpEntity<*>>(),
            eq(GoogleUserInfoResponse::class.java)
        )
    }
}