package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@DisplayName("NaverOAuthClient 테스트")
class NaverOAuthClientTest {
    private val restTemplate = mock<RestTemplate>()
    private lateinit var naverOAuthClient: NaverOAuthClient

    @BeforeEach
    fun setUp() {
        naverOAuthClient = NaverOAuthClient(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
            redirectUri = "http://localhost/callback",
            restTemplate = restTemplate
        )
    }

    @Test
    @DisplayName("네이버 사용자 정보를 성공적으로 가져온다")
    fun shouldGetUserInfoSuccessfully() {
        // Given
        val accessToken = "naver-access-token"
        val userInfoResponse = NaverUserInfoResponse(
            response = NaverUserInfoResponse.NaverUser(
                id = "naver-user-id",
                email = "naver@example.com",
                name = "네이버유저",
                profileImage = "profile.jpg"
            )
        )
        val responseEntity = ResponseEntity.ok(userInfoResponse)
        doReturn(responseEntity).whenever(restTemplate)
            .exchange(any<String>(), eq(HttpMethod.GET), any(), eq(NaverUserInfoResponse::class.java))

        // When
        val result = naverOAuthClient.getUserInfo(accessToken)

        // Then
        assertEquals("naver-user-id", result.id)
        assertEquals("naver@example.com", result.email)
        assertEquals("네이버유저", result.name)
    }
}
