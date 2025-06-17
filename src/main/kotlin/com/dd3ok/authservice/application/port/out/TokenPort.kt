package com.dd3ok.authservice.application.port.out

import com.dd3ok.authservice.domain.vo.UserId

interface TokenPort {
    fun generateAccessToken(userId: UserId): String
    fun generateRefreshToken(userId: UserId): String
    fun validateToken(token: String): TokenValidationResult
    fun getUserIdFromToken(token: String): UserId
    fun revokeToken(token: String)
}

data class TokenValidationResult(
    val isValid: Boolean,
    val reason: String? = null
)
