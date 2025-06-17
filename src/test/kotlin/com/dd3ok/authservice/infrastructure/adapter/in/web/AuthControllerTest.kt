package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import com.dd3ok.authservice.application.port.`in`.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AuthController::class])
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    // TestConfig를 내부 클래스로 이동하여 @Import 제거
    @TestConfiguration
    class TestConfig {
        @Bean
        @Primary
        fun authenticationUseCase(): AuthenticationUseCase = mock()

        @Bean
        @Primary
        fun userRegistrationUseCase(): UserRegistrationUseCase = mock()

        @Bean
        @Primary
        fun testSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .csrf { it.disable() }
                .authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var authenticationUseCase: AuthenticationUseCase

    @Test
    @DisplayName("OAuth 로그인 API를 호출할 수 있다")
    fun shouldAuthenticateWithOAuth() {
        // Given
        val request = OAuthLoginRequest(
            code = "auth-code-123",
            redirectUri = "http://localhost:8080/callback"
        )

        val mockResponse = AuthenticationResponse(
            user = UserResponse(1L, "test@example.com", "testuser", listOf("Google")),
            tokens = TokenResponse("access-token", "refresh-token", "Bearer", 1800)
        )

        whenever(authenticationUseCase.authenticateWithOAuth(any())).thenReturn(mockResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/oauth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.user.userId").value(1L))
    }

    @Test
    @DisplayName("토큰 갱신 API를 호출할 수 있다")
    fun shouldRefreshToken() {
        // Given
        val request = RefreshTokenRequest("refresh-token-123")
        val mockResponse = TokenResponse("new-access-token", "new-refresh-token", "Bearer", 1800)

        whenever(authenticationUseCase.refreshToken(any())).thenReturn(mockResponse)

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
    }

    @Test
    @DisplayName("로그아웃 API를 호출할 수 있다")
    fun shouldLogout() {
        // Given
        val request = LogoutRequest(userId = 1L)

        doNothing().whenever(authenticationUseCase).logout(any())

        // When & Then
        mockMvc.perform(
            post("/api/v1/auth/logout")
                .header("Authorization", "Bearer access-token-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNoContent)
    }
}
