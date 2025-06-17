package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import com.dd3ok.authservice.application.port.`in`.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationUseCase: AuthenticationUseCase,
    private val userRegistrationUseCase: UserRegistrationUseCase
) {

    @PostMapping("/oauth/{provider}")
    fun authenticateWithOAuth(
        @PathVariable provider: String,
        @Valid @RequestBody request: OAuthLoginRequest
    ): ResponseEntity<AuthenticationResponse> {
        val command = OAuthAuthenticationCommand(
            provider = provider,
            authorizationCode = request.code,
            redirectUri = request.redirectUri,
            state = request.state
        )

        val response = authenticationUseCase.authenticateWithOAuth(command)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<TokenResponse> {
        val command = RefreshTokenCommand(request.refreshToken)
        val response = authenticationUseCase.refreshToken(command)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody request: LogoutRequest,
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {
        val accessToken = authorization.removePrefix("Bearer ")
        val command = LogoutCommand(
            userId = request.userId,
            accessToken = accessToken
        )

        authenticationUseCase.logout(command)
        return ResponseEntity.noContent().build()
    }
}

data class OAuthLoginRequest(
    val code: String,
    val redirectUri: String? = null,
    val state: String? = null
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val userId: Long
)
