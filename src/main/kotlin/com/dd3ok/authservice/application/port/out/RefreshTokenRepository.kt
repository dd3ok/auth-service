package com.dd3ok.authservice.application.port.out

import com.dd3ok.authservice.domain.model.RefreshToken
import com.dd3ok.authservice.domain.vo.UserId

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByToken(token: String): RefreshToken?
    fun findByUserId(userId: UserId): List<RefreshToken>
    fun deleteByToken(token: String)
    fun deleteByUserId(userId: UserId)
}
