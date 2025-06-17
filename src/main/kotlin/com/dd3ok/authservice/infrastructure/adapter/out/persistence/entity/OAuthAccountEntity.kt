package com.dd3ok.authservice.infrastructure.adapter.out.persistence.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "oauth_accounts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "oauth_id"]),
        UniqueConstraint(columnNames = ["user_id", "provider"])
    ]
)
class OAuthAccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var provider: OAuthProviderEntity,
    
    @Column(name = "oauth_id", nullable = false)
    var oauthId: String,
    
    @Column(name = "oauth_email", nullable = false)
    var oauthEmail: String
)

enum class OAuthProviderEntity {
    GOOGLE, KAKAO, NAVER
}
