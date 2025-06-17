package com.dd3ok.authservice.domain.vo

enum class OAuthProvider(val displayName: String, val clientName: String) {
    GOOGLE("Google", "google"),
    KAKAO("Kakao", "kakao"),
    NAVER("Naver", "naver");

    companion object {
        fun fromClientName(clientName: String): OAuthProvider {
            return values().find { it.clientName == clientName }
                ?: throw IllegalArgumentException("Unknown OAuth provider: $clientName")
        }
    }
}