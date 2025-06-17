package com.dd3ok.authservice.infrastructure.adapter.out.token

import com.dd3ok.authservice.application.port.out.RefreshTokenRepository
import com.dd3ok.authservice.domain.model.RefreshToken
import com.dd3ok.authservice.domain.vo.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration
import java.time.LocalDateTime

@Repository
class RedisRefreshTokenRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : RefreshTokenRepository {
    
    companion object {
        private const val REFRESH_TOKEN_PREFIX = "refresh_token:"
        private const val USER_TOKENS_PREFIX = "user_tokens:"
    }
    
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val key = REFRESH_TOKEN_PREFIX + refreshToken.token
        val userTokensKey = USER_TOKENS_PREFIX + refreshToken.userId.value
        
        // RefreshToken 저장
        val json = objectMapper.writeValueAsString(refreshToken)
        val ttl = Duration.between(LocalDateTime.now(), refreshToken.expiresAt)
        
        redisTemplate.opsForValue().set(key, json, ttl)
        
        // 사용자별 토큰 목록에 추가
        redisTemplate.opsForSet().add(userTokensKey, refreshToken.token)
        redisTemplate.expire(userTokensKey, ttl)
        
        return refreshToken
    }
    
    override fun findByToken(token: String): RefreshToken? {
        val key = REFRESH_TOKEN_PREFIX + token
        val json = redisTemplate.opsForValue().get(key)
        
        return json?.let {
            objectMapper.readValue(it, RefreshToken::class.java)
        }
    }
    
    override fun findByUserId(userId: UserId): List<RefreshToken> {
        val userTokensKey = USER_TOKENS_PREFIX + userId.value
        val tokens = redisTemplate.opsForSet().members(userTokensKey) ?: emptySet()
        
        return tokens.mapNotNull { token ->
            findByToken(token)
        }
    }
    
    override fun deleteByToken(token: String) {
        val key = REFRESH_TOKEN_PREFIX + token
        redisTemplate.delete(key)
    }
    
    override fun deleteByUserId(userId: UserId) {
        val userTokensKey = USER_TOKENS_PREFIX + userId.value
        val tokens = redisTemplate.opsForSet().members(userTokensKey) ?: emptySet()
        
        tokens.forEach { token ->
            deleteByToken(token)
        }
        
        redisTemplate.delete(userTokensKey)
    }
}
