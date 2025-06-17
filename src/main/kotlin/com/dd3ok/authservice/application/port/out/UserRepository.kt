package com.dd3ok.authservice.application.port.out

import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun findByOAuthAccount(provider: OAuthProvider, oauthId: String): User?
    fun existsByEmail(email: Email): Boolean
    fun delete(user: User)
}
