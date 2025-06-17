package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import com.dd3ok.authservice.application.port.`in`.LinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.`in`.UnlinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.`in`.UpdateUserCommand
import com.dd3ok.authservice.application.port.`in`.UserRegistrationUseCase
import com.dd3ok.authservice.application.port.`in`.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userRegistrationUseCase: UserRegistrationUseCase
) {
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userId: Long): ResponseEntity<UserResponse> {
        val user = userRegistrationUseCase.getCurrentUser(userId)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/me")
    fun updateCurrentUser(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val command = UpdateUserCommand(userId, request.nickname)
        val updatedUser = userRegistrationUseCase.updateUser(command)
        return ResponseEntity.ok(updatedUser)
    }

    @PostMapping("/oauth/link")
    fun linkOAuthAccount(
        @Valid @RequestBody request: LinkOAuthRequest,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<UserResponse> {
        val command = LinkOAuthAccountCommand(
            userId = userId,
            provider = request.provider,
            oauthId = request.oauthId,
            email = request.email
        )

        val response = userRegistrationUseCase.linkOAuthAccount(command)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/oauth/{provider}")
    fun unlinkOAuthAccount(
        @PathVariable provider: String,
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<UserResponse> {
        val command = UnlinkOAuthAccountCommand(
            userId = userId,
            provider = provider
        )

        val response = userRegistrationUseCase.unlinkOAuthAccount(command)
        return ResponseEntity.ok(response)
    }
}

data class LinkOAuthRequest(
    val provider: String,
    val oauthId: String,
    val email: String
)

data class UpdateUserRequest(
    val nickname: String
)