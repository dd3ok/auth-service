// src/main/kotlin/com/portfolio/oauth/infrastructure/adapter/out/persistence/UserJpaRepository.kt
package com.dd3ok.authservice.infrastructure.adapter.out.persistence

import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.OAuthProviderEntity
import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    
    fun findByEmail(email: String): UserEntity?
    
    fun existsByEmail(email: String): Boolean
    
    @Query("""
        SELECT u FROM UserEntity u 
        JOIN u.oauthAccounts oa 
        WHERE oa.provider = :provider AND oa.oauthId = :oauthId
    """)
    fun findByOAuthAccount(
        @Param("provider") provider: OAuthProviderEntity,
        @Param("oauthId") oauthId: String
    ): UserEntity?
}
