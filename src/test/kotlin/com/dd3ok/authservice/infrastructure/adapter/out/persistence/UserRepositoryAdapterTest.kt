package com.dd3ok.authservice.infrastructure.adapter.out.persistence

import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용
@Import(UserRepositoryAdapter::class, UserMapper::class)
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryAdapterTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    private fun createTestUser(): User {
        val oauthAccount = OAuthAccount(
            provider = OAuthProvider.GOOGLE,
            oauthId = "google-123",
            email = "test@gmail.com"
        )

        return User.createFromOAuth(
            email = Email("test@example.com"),
            nickname = "testuser",
            oauthAccount = oauthAccount
        )
    }

    @Test
    @DisplayName("사용자를 저장하고 조회할 수 있다")
    fun shouldSaveAndFindUser() {
        // Given
        val user = createTestUser()

        // When
        val savedUser = userRepository.save(user)
        val foundUser = userRepository.findById(savedUser.id!!)

        // Then
        assertNotNull(foundUser)  // null check 먼저
        assertEquals(savedUser.email, foundUser!!.email)  // !! 추가
        assertEquals(savedUser.nickname, foundUser.nickname)
        assertEquals(1, foundUser.oauthAccounts.size)
    }

    @Test
    @DisplayName("이메일로 사용자를 찾을 수 있다")
    fun shouldFindUserByEmail() {
        // Given
        val user = createTestUser()
        userRepository.save(user)

        // When
        val foundUser = userRepository.findByEmail(user.email)

        // Then
        assertNotNull(foundUser)
        assertEquals(user.email, foundUser!!.email)  // !! 추가
    }

    @Test
    @DisplayName("OAuth 계정으로 사용자를 찾을 수 있다")
    fun shouldFindUserByOAuthAccount() {
        // Given
        val user = createTestUser()
        userRepository.save(user)
        val oauthAccount = user.oauthAccounts.first()

        // When
        val foundUser = userRepository.findByOAuthAccount(
            oauthAccount.provider,
            oauthAccount.oauthId
        )

        // Then
        assertNotNull(foundUser)
        assertEquals(user.email, foundUser!!.email)  // !! 추가
        assertTrue(foundUser.hasOAuthProvider(OAuthProvider.GOOGLE))
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 null을 반환한다")
    fun shouldReturnNullForNonExistentUser() {
        // When
        val foundUser = userRepository.findById(UserId.of(999L))

        // Then
        assertNull(foundUser)
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    fun shouldCheckEmailExists() {
        // Given
        val user = createTestUser()
        userRepository.save(user)

        // When & Then
        assertTrue(userRepository.existsByEmail(user.email))
        assertTrue(!userRepository.existsByEmail(Email("nonexistent@example.com")))
    }
}
