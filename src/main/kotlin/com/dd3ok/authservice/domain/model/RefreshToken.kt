package com.dd3ok.authservice.domain.model

import com.dd3ok.authservice.domain.vo.UserId
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class RefreshToken @JsonCreator constructor(
    @JsonProperty("token") val token: String,
    @JsonProperty("userId") val userId: UserId,
    @JsonProperty("expiresAt") val expiresAt: LocalDateTime,
    @JsonProperty("createdAt") val createdAt: LocalDateTime = LocalDateTime.now(),
    @JsonProperty("isRevoked") val isRevoked: Boolean = false
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
    fun isValid(): Boolean = !isRevoked && !isExpired()
    fun revoke(): RefreshToken = this.copy(isRevoked = true)
}