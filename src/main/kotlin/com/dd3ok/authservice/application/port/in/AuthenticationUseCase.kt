package com.dd3ok.authservice.application.port.`in`

interface AuthenticationUseCase {
    fun authenticateWithOAuth(command: OAuthAuthenticationCommand): AuthenticationResponse
    fun refreshToken(command: RefreshTokenCommand): TokenResponse
    fun logout(command: LogoutCommand)
}

data class OAuthAuthenticationCommand(
    val provider: String,
    val authorizationCode: String,
    val redirectUri: String? = null,
    val state: String? = null
)

data class RefreshTokenCommand(
    val refreshToken: String
)

data class LogoutCommand(
    val userId: Long,
    val accessToken: String
)

data class AuthenticationResponse(
    val user: UserResponse,
    val tokens: TokenResponse
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)
