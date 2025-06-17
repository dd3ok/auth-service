package com.dd3ok.authservice.domain.model

import com.dd3ok.authservice.domain.vo.UserId
import java.time.LocalDateTime

data class RefreshToken(
    val token: String,
    val userId: UserId,
    val expiresAt: LocalDateTime,
    val isRevoked: Boolean = false
) {
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
    
    fun revoke(): RefreshToken {
        return this.copy(isRevoked = true)
    }
    
    fun isValid(): Boolean {
        return !isRevoked && !isExpired()
    }
}
