package com.dd3ok.authservice.infrastructure.adapter.out.persistence

import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.OAuthAccountEntity
import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.OAuthProviderEntity
import com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDomain(entity: UserEntity): User {
        return User(
            id = entity.id?.let { UserId.Companion.of(it) },
            email = Email(entity.email),
            nickname = entity.nickname,
            oauthAccounts = entity.oauthAccounts.map { it.toDomain() }.toMutableSet(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: User): UserEntity {
        val entity = UserEntity(
            id = domain.id?.value,
            email = domain.email.value,
            nickname = domain.nickname,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )

        // OAuth 계정들을 엔티티로 변환
        val oauthAccountEntities = domain.oauthAccounts.map { oauthAccount ->
            OAuthAccountEntity(
                user = entity,
                provider = oauthAccount.provider.toEntity(),
                oauthId = oauthAccount.oauthId,
                oauthEmail = oauthAccount.email
            )
        }.toMutableSet()

        entity.oauthAccounts = oauthAccountEntities
        return entity
    }

    private fun OAuthAccountEntity.toDomain(): OAuthAccount {
        return OAuthAccount(
            provider = this.provider.toDomain(),
            oauthId = this.oauthId,
            email = this.oauthEmail
        )
    }

    private fun OAuthProvider.toEntity(): OAuthProviderEntity {
        return when (this) {
            OAuthProvider.GOOGLE -> OAuthProviderEntity.GOOGLE
            OAuthProvider.KAKAO -> OAuthProviderEntity.KAKAO
            OAuthProvider.NAVER -> OAuthProviderEntity.NAVER
        }
    }

    private fun OAuthProviderEntity.toDomain(): OAuthProvider {
        return when (this) {
            OAuthProviderEntity.GOOGLE -> OAuthProvider.GOOGLE
            OAuthProviderEntity.KAKAO -> OAuthProvider.KAKAO
            OAuthProviderEntity.NAVER -> OAuthProvider.NAVER
        }
    }
}