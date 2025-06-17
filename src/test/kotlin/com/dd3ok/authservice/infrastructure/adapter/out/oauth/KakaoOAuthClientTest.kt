package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@DisplayName("KakaoOAuthClient 테스트")
class KakaoOAuthClientTest {
    private val restTemplate = mock<RestTemplate>()
    private lateinit var kakaoOAuthClient: KakaoOAuthClient

    @BeforeEach
    fun setUp() {
        kakaoOAuthClient = KakaoOAuthClient(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            redirectUri = "http://localhost/callback",
            restTemplate = restTemplate
        )
    }

    @Test
    @DisplayName("카카오 사용자 정보를 성공적으로 가져온다")
    fun shouldGetUserInfoSuccessfully() {
        // Given
        val accessToken = "kakao-access-token"
        val userInfoResponse = KakaoUserInfoResponse(
            id = 12345L,
            kakaoAccount = KakaoUserInfoResponse.KakaoAccount(
                email = "kakao@example.com",
                profile = KakaoUserInfoResponse.KakaoAccount.Profile("카카오유저", "profile.jpg")
            )
        )
        val responseEntity = ResponseEntity.ok(userInfoResponse)
        doReturn(responseEntity).whenever(restTemplate)
            .exchange(any<String>(), eq(HttpMethod.GET), any(), eq(KakaoUserInfoResponse::class.java))

        // When
        val result = kakaoOAuthClient.getUserInfo(accessToken)

        // Then
        assertEquals("12345", result.id)
        assertEquals("kakao@example.com", result.email)
        assertEquals("카카오유저", result.name)
    }
}
