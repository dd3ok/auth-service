package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.domain.vo.UserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("JwtTokenService 테스트")
class JwtTokenServiceTest {

    private lateinit var jwtTokenService: JwtTokenService

    @BeforeEach
    fun setUp() {
        jwtTokenService = JwtTokenService(
            secretKey = "testSecretKeyForJWTTokenGenerationThatNeedsToBeVeryLongForSecurity",
            accessTokenExpiration = 1800000L,  // 30분
            refreshTokenExpiration = 1209600000L  // 2주
        )
    }

    @Test
    @DisplayName("Access Token을 생성할 수 있다")
    fun shouldGenerateAccessToken() {
        // Given
        val userId = UserId.of(1L)

        // When
        val token = jwtTokenService.generateAccessToken(userId)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertEquals("access", jwtTokenService.getTokenType(token))
    }

    @Test
    @DisplayName("Refresh Token을 생성할 수 있다")
    fun shouldGenerateRefreshToken() {
        // Given
        val userId = UserId.of(1L)

        // When
        val token = jwtTokenService.generateRefreshToken(userId)

        // Then
        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertEquals("refresh", jwtTokenService.getTokenType(token))
    }

    @Test
    @DisplayName("유효한 토큰을 검증할 수 있다")
    fun shouldValidateValidToken() {
        // Given
        val userId = UserId.of(1L)
        val token = jwtTokenService.generateAccessToken(userId)

        // When
        val result = jwtTokenService.validateToken(token)

        // Then
        assertTrue(result.isValid)
    }

    @Test
    @DisplayName("잘못된 토큰은 검증에 실패한다")
    fun shouldFailValidationForInvalidToken() {
        // Given
        val invalidToken = "invalid.token.here"

        // When
        val result = jwtTokenService.validateToken(invalidToken)

        // Then
        assertFalse(result.isValid)
        assertNotNull(result.reason)
    }

    @Test
    @DisplayName("토큰에서 사용자 ID를 추출할 수 있다")
    fun shouldExtractUserIdFromToken() {
        // Given
        val userId = UserId.of(123L)
        val token = jwtTokenService.generateAccessToken(userId)

        // When
        val extractedUserId = jwtTokenService.getUserIdFromToken(token)

        // Then
        assertEquals(userId, extractedUserId)
    }
}