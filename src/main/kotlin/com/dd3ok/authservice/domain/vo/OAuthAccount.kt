package com.dd3ok.authservice.domain.vo

data class OAuthAccount(
    val provider: OAuthProvider,
    val oauthId: String,
    val email: String
) {
    init {
        require(oauthId.isNotBlank()) { "OAuth ID cannot be blank" }
        require(Email.isValid(email)) { "Invalid email in OAuth account: $email" }
    }
}
