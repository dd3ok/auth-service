package com.dd3ok.authservice.domain.model

import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import java.time.LocalDateTime
import kotlin.collections.find

data class User(
    val id: UserId?,
    val email: Email,
    val nickname: String,
    val oauthAccounts: MutableSet<OAuthAccount> = mutableSetOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(nickname.isNotBlank()) { "Nickname cannot be blank" }
        require(nickname.length in 2..20) { "Nickname must be between 2 and 20 characters" }
    }

    fun linkOAuthAccount(account: OAuthAccount): User {
        // 같은 provider의 계정이 이미 연결되어 있는지 확인
        val existingAccount = oauthAccounts.find { it.provider == account.provider }
        if (existingAccount != null) {
            throw IllegalStateException("${account.provider.displayName} account is already linked")
        }

        oauthAccounts.add(account)
        return this.copy(updatedAt = LocalDateTime.now())
    }

    fun unlinkOAuthAccount(provider: OAuthProvider): User {
        val accountToRemove = oauthAccounts.find { it.provider == provider }
            ?: throw IllegalStateException("No ${provider.displayName} account found to unlink")

        oauthAccounts.remove(accountToRemove)
        return this.copy(updatedAt = LocalDateTime.now())
    }

    fun hasOAuthProvider(provider: OAuthProvider): Boolean {
        return oauthAccounts.any { it.provider == provider }
    }

    fun getOAuthAccount(provider: OAuthProvider): OAuthAccount? {
        return oauthAccounts.find { it.provider == provider }
    }

    fun isNewUser(): Boolean {
        return id == null
    }

    fun withId(userId: UserId): User {
        return this.copy(id = userId)
    }

    companion object {
        fun createFromOAuth(
            email: Email,
            nickname: String,
            oauthAccount: OAuthAccount
        ): User {
            val user = User(
                id = null, // 새 사용자는 ID가 없음
                email = email,
                nickname = nickname
            )
            return user.linkOAuthAccount(oauthAccount)
        }
    }
}
