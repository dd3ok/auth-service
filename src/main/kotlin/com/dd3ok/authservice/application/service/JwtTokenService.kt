package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.application.port.out.TokenPort
import com.dd3ok.authservice.application.port.out.TokenValidationResult
import com.dd3ok.authservice.domain.vo.UserId
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey


@Service
class JwtTokenService(
    @Value("\${app.jwt.secret}") private val secretKey: String,
    @Value("\${app.jwt.access-token.expiration}") private val accessTokenExpiration: Long,
    @Value("\${app.jwt.refresh-token.expiration}") private val refreshTokenExpiration: Long
) : TokenPort {

    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    override fun generateAccessToken(userId: UserId): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.value.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "access")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    override fun generateRefreshToken(userId: UserId): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.value.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("type", "refresh")
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    override fun validateToken(token: String): TokenValidationResult {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)

            return TokenValidationResult(true)
        } catch (ex: ExpiredJwtException) {
            return TokenValidationResult(false, "Token expired")
        } catch (ex: JwtException) {
            return TokenValidationResult(false, "Invalid token: ${ex.message}")
        } catch (ex: Exception) {
            return TokenValidationResult(false, "Token validation error: ${ex.message}")
        }
    }

    override fun getUserIdFromToken(token: String): UserId {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

        val userIdValue = claims.subject.toLong()
        return UserId.of(userIdValue)
    }

    override fun revokeToken(token: String) {
        // 구현 생략
    }

    fun getTokenType(token: String): String? {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body

            claims.get("type") as? String
        } catch (ex: Exception) {
            null
        }
    }
}
