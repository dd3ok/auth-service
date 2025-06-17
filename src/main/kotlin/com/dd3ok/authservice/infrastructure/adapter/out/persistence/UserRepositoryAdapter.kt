package com.dd3ok.authservice.infrastructure.adapter.out.persistence

import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.OAuthProviderEntity
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val userMapper: UserMapper
) : UserRepository {
    
    override fun save(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = userJpaRepository.save(entity)
        return userMapper.toDomain(savedEntity)
    }
    
    override fun findById(id: UserId): User? {
        return userJpaRepository.findById(id.value)
            .map { userMapper.toDomain(it) }
            .orElse(null)
    }
    
    override fun findByEmail(email: Email): User? {
        return userJpaRepository.findByEmail(email.value)
            ?.let { userMapper.toDomain(it) }
    }
    
    override fun findByOAuthAccount(provider: OAuthProvider, oauthId: String): User? {
        val providerEntity = provider.toEntity()
        return userJpaRepository.findByOAuthAccount(providerEntity, oauthId)
            ?.let { userMapper.toDomain(it) }
    }
    
    override fun existsByEmail(email: Email): Boolean {
        return userJpaRepository.existsByEmail(email.value)
    }
    
    override fun delete(user: User) {
        user.id?.let { userId ->
            userJpaRepository.deleteById(userId.value)
        }
    }
    
    private fun OAuthProvider.toEntity(): OAuthProviderEntity {
        return when (this) {
            OAuthProvider.GOOGLE -> OAuthProviderEntity.GOOGLE
            OAuthProvider.KAKAO -> OAuthProviderEntity.KAKAO
            OAuthProvider.NAVER -> OAuthProviderEntity.NAVER
        }
    }
}
