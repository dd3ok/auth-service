package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.application.port.`in`.LogoutCommand
import com.dd3ok.authservice.application.port.`in`.OAuthAuthenticationCommand
import com.dd3ok.authservice.application.port.`in`.RefreshTokenCommand
import com.dd3ok.authservice.application.port.`in`.UserRegistrationResponse
import com.dd3ok.authservice.application.port.out.OAuthClientPort
import com.dd3ok.authservice.application.port.out.OAuthTokenResponse
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.dd3ok.authservice.application.port.out.RefreshTokenRepository
import com.dd3ok.authservice.application.port.out.TokenPort
import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.exception.InvalidOAuthStateException
import com.dd3ok.authservice.domain.model.RefreshToken
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime


@DisplayName("AuthenticationService 테스트")
class AuthenticationServiceTest {

    private val userRepository = mock<UserRepository>()
    private val oauthClientPort = mock<OAuthClientPort>()
    private val tokenPort = mock<TokenPort>()
    private val refreshTokenRepository = mock<RefreshTokenRepository>()
    private val userRegistrationService = mock<UserRegistrationService>()

    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setUp() {
        authenticationService = AuthenticationService(
            userRepository,
            oauthClientPort,
            tokenPort,
            refreshTokenRepository,
            userRegistrationService
        )
    }

    private fun createTestUser(): User {
        return User(
            id = UserId.of(1L),
            email = Email("test@example.com"),
            nickname = "testuser"
        ).linkOAuthAccount(
            OAuthAccount(
                provider = OAuthProvider.GOOGLE,
                oauthId = "google-123",
                email = "test@gmail.com"
            )
        )
    }

    private fun createOAuthTokenResponse(): OAuthTokenResponse {
        return OAuthTokenResponse(
            accessToken = "oauth-access-token",
            refreshToken = "oauth-refresh-token",
            tokenType = "Bearer",
            expiresIn = 3600
        )
    }

    private fun createOAuthUserInfo(): OAuthUserInfoResponse {
        return OAuthUserInfoResponse(
            id = "google-123",
            email = "test@example.com",
            name = "Test User",
            profileImageUrl = "https://example.com/profile.jpg"
        )
    }

    @Test
    @DisplayName("기존 사용자 OAuth 인증을 성공적으로 처리할 수 있다")
    fun shouldAuthenticateExistingOAuthUser() {
        // Given
        val command = OAuthAuthenticationCommand(
            provider = "google",
            authorizationCode = "auth-code-123",
            redirectUri = "http://localhost:8080/callback",
            state = "some-state" // state 추가
        )

        val user = createTestUser()
        val oauthTokenResponse = createOAuthTokenResponse()
        val oauthUserInfo = createOAuthUserInfo()

        // ✨ 수정된 부분: getAccessToken에 state 파라미터 추가
        doReturn(oauthTokenResponse).whenever(oauthClientPort)
            .getAccessToken(OAuthProvider.GOOGLE, "auth-code-123", command.redirectUri, command.state)

        doReturn(oauthUserInfo).whenever(oauthClientPort)
            .getUserInfo(OAuthProvider.GOOGLE, "oauth-access-token")
        doReturn(user).whenever(userRepository)
            .findByOAuthAccount(OAuthProvider.GOOGLE, "google-123")
        doReturn("jwt-access-token").whenever(tokenPort)
            .generateAccessToken(UserId.of(1L))
        doReturn("jwt-refresh-token").whenever(tokenPort)
            .generateRefreshToken(UserId.of(1L))

        // When
        val response = authenticationService.authenticateWithOAuth(command)

        // Then
        assertEquals(1L, response.user.userId)
        assertEquals("test@example.com", response.user.email)
        assertEquals("jwt-access-token", response.tokens.accessToken)
        assertEquals("jwt-refresh-token", response.tokens.refreshToken)

        verify(refreshTokenRepository).save(any())
        verify(userRegistrationService, never()).registerWithOAuth(any())
    }

    @Test
    @DisplayName("신규 사용자 OAuth 인증 시 자동 가입 처리할 수 있다")
    fun shouldRegisterAndAuthenticateNewOAuthUser() {
        // Given
        val command = OAuthAuthenticationCommand(
            provider = "google",
            authorizationCode = "auth-code-123",
            state = "some-state" // state 추가
        )

        val oauthTokenResponse = createOAuthTokenResponse()
        val oauthUserInfo = createOAuthUserInfo()
        val registrationResponse = UserRegistrationResponse(
            userId = 2L,
            email = "test@example.com",
            nickname = "testuser",
            isNewUser = true
        )
        val newUser = createTestUser().withId(UserId.of(2L))

        // ✨ 수정된 부분: getAccessToken에 state 파라미터 추가
        doReturn(oauthTokenResponse).whenever(oauthClientPort)
            .getAccessToken(OAuthProvider.GOOGLE, "auth-code-123", null, command.state)

        doReturn(oauthUserInfo).whenever(oauthClientPort)
            .getUserInfo(OAuthProvider.GOOGLE, "oauth-access-token")
        doReturn(null).whenever(userRepository)
            .findByOAuthAccount(OAuthProvider.GOOGLE, "google-123")
        doReturn(registrationResponse).whenever(userRegistrationService)
            .registerWithOAuth(any())
        doReturn(newUser).whenever(userRepository)
            .findById(UserId.of(2L))
        doReturn("jwt-access-token").whenever(tokenPort)
            .generateAccessToken(UserId.of(2L))
        doReturn("jwt-refresh-token").whenever(tokenPort)
            .generateRefreshToken(UserId.of(2L))

        // When
        val response = authenticationService.authenticateWithOAuth(command)

        // Then
        assertEquals(2L, response.user.userId)
        verify(userRegistrationService).registerWithOAuth(any())
        verify(refreshTokenRepository).save(any())
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 토큰을 갱신할 수 있다")
    fun shouldRefreshTokenWithValidRefreshToken() {
        // Given
        val command = RefreshTokenCommand("valid-refresh-token")
        val refreshToken = RefreshToken(
            token = "valid-refresh-token",
            userId = UserId.of(1L),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        val user = createTestUser()

        doReturn(refreshToken).whenever(refreshTokenRepository)
            .findByToken("valid-refresh-token")
        doReturn(user).whenever(userRepository)
            .findById(UserId.of(1L))
        doReturn("new-access-token").whenever(tokenPort)
            .generateAccessToken(UserId.of(1L))
        doReturn("new-refresh-token").whenever(tokenPort)
            .generateRefreshToken(UserId.of(1L))

        // When
        val response = authenticationService.refreshToken(command)

        // Then
        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)

        verify(refreshTokenRepository).deleteByToken("valid-refresh-token")
        verify(refreshTokenRepository).save(any())
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 갱신 시 예외가 발생한다")
    fun shouldThrowExceptionForInvalidRefreshToken() {
        // Given
        val command = RefreshTokenCommand("invalid-refresh-token")

        doReturn(null).whenever(refreshTokenRepository)
            .findByToken("invalid-refresh-token")

        // When & Then
        assertThrows<InvalidOAuthStateException> {
            authenticationService.refreshToken(command)
        }
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 갱신 시 예외가 발생한다")
    fun shouldThrowExceptionForExpiredRefreshToken() {
        // Given
        val command = RefreshTokenCommand("expired-refresh-token")
        val expiredRefreshToken = RefreshToken(
            token = "expired-refresh-token",
            userId = UserId.of(1L),
            expiresAt = LocalDateTime.now().minusDays(1) // 만료된 토큰
        )

        doReturn(expiredRefreshToken).whenever(refreshTokenRepository)
            .findByToken("expired-refresh-token")

        // When & Then
        assertThrows<InvalidOAuthStateException> {
            authenticationService.refreshToken(command)
        }
    }

    @Test
    @DisplayName("로그아웃 시 토큰들을 무효화할 수 있다")
    fun shouldRevokeTokensOnLogout() {
        // Given
        val command = LogoutCommand(
            userId = 1L,
            accessToken = "access-token-to-revoke"
        )

        // When
        authenticationService.logout(command)

        // Then
        verify(tokenPort).revokeToken("access-token-to-revoke")
        verify(refreshTokenRepository).deleteByUserId(UserId.of(1L))
    }
}