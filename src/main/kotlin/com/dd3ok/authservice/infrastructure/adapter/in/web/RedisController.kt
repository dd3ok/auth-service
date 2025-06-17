package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import com.dd3ok.authservice.application.port.out.RefreshTokenRepository
import com.dd3ok.authservice.domain.vo.UserId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/redis")
class RedisController(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    @GetMapping("/refresh-tokens")
    fun getRedisRefreshTokens(@AuthenticationPrincipal userId: Long): ResponseEntity<RedisDebugInfo> {
        val userTokens = refreshTokenRepository.findByUserId(UserId.of(userId))
        val redisKeys = redisTemplate.keys("refresh_token:*").toList()
        val userRedisKeys = redisTemplate.keys("user_tokens:$userId").toList()
        
        return ResponseEntity.ok(RedisDebugInfo(
            userTokenCount = userTokens.size,
            totalRedisTokenKeys = redisKeys.size,
            userRedisKeys = userRedisKeys,
            userTokens = userTokens.map { 
                RefreshTokenInfo(
                    token = it.token.take(20) + "...", // 보안을 위해 일부만 표시
                    expiresAt = it.expiresAt.toString(),
                    isValid = it.isValid()
                )
            }
        ))
    }
    
    @DeleteMapping("/refresh-tokens")
    fun clearUserRefreshTokens(@AuthenticationPrincipal userId: Long): ResponseEntity<String> {
        refreshTokenRepository.deleteByUserId(UserId.of(userId))
        return ResponseEntity.ok("사용자의 모든 Refresh Token이 Redis에서 삭제되었습니다.")
    }
}

data class RedisDebugInfo(
    val userTokenCount: Int,
    val totalRedisTokenKeys: Int,
    val userRedisKeys: List<String>,
    val userTokens: List<RefreshTokenInfo>
)

data class RefreshTokenInfo(
    val token: String,
    val expiresAt: String,
    val isValid: Boolean
)
