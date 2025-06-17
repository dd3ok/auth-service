package com.dd3ok.authservice.application.port.`in`

interface UserRegistrationUseCase {
    fun registerWithOAuth(command: RegisterWithOAuthCommand): UserRegistrationResponse
    fun linkOAuthAccount(command: LinkOAuthAccountCommand): UserResponse
    fun unlinkOAuthAccount(command: UnlinkOAuthAccountCommand): UserResponse
}

data class RegisterWithOAuthCommand(
    val provider: String,
    val oauthId: String,
    val email: String,
    val nickname: String
)

data class LinkOAuthAccountCommand(
    val userId: Long,
    val provider: String,
    val oauthId: String,
    val email: String
)

data class UnlinkOAuthAccountCommand(
    val userId: Long,
    val provider: String
)

data class UserRegistrationResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val isNewUser: Boolean
)

data class UserResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val linkedProviders: List<String>
)
