package com.dd3ok.authservice.domain.model

import com.dd3ok.authservice.domain.vo.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("User 도메인 테스트")
class UserTest {

    private fun createTestUser(): User {
        return User(
            id = UserId.of(1L),
            email = Email("test@example.com"),
            nickname = "testuser"
        )
    }

    private fun createNewUser(): User {
        return User(
            id = null,
            email = Email("newuser@example.com"),
            nickname = "newuser"
        )
    }

    private fun createGoogleOAuthAccount(): OAuthAccount {
        return OAuthAccount(
            provider = OAuthProvider.GOOGLE,
            oauthId = "google-123456",
            email = "test@gmail.com"
        )
    }

    @Test
    @DisplayName("유효한 데이터로 사용자를 생성할 수 있다")
    fun shouldCreateUserWithValidData() {
        val user = createTestUser()

        assertEquals("test@example.com", user.email.value)
        assertEquals("testuser", user.nickname)
        assertTrue(user.oauthAccounts.isEmpty())
        assertFalse(user.isNewUser())
    }

    @Test
    @DisplayName("ID없이 신규 사용자를 생성할 수 있다")
    fun shouldCreateNewUserWithoutId() {
        val user = createNewUser()

        assertTrue(user.isNewUser())
        assertEquals(null, user.id)
    }

    @Test
    @DisplayName("신규 사용자에게 ID를 할당할 수 있다")
    fun shouldAssignIdToNewUser() {
        val newUser = createNewUser()
        val userWithId = newUser.withId(UserId.of(100L))

        assertEquals(UserId.of(100L), userWithId.id)
        assertFalse(userWithId.isNewUser())
    }

    @Test
    @DisplayName("닉네임이 공백이면 예외가 발생한다")
    fun shouldThrowExceptionWhenNicknameIsBlank() {
        val exception = assertThrows<IllegalArgumentException> {
            User(
                id = UserId.of(1L),
                email = Email("test@example.com"),
                nickname = ""
            )
        }
        assertEquals("Nickname cannot be blank", exception.message)
    }

    @Test
    @DisplayName("닉네임이 너무 짧으면 예외가 발생한다")
    fun shouldThrowExceptionWhenNicknameIsTooShort() {
        val exception = assertThrows<IllegalArgumentException> {
            User(
                id = UserId.of(1L),
                email = Email("test@example.com"),
                nickname = "a"
            )
        }
        assertEquals("Nickname must be between 2 and 20 characters", exception.message)
    }

    @Test
    @DisplayName("닉네임이 너무 길면 예외가 발생한다")
    fun shouldThrowExceptionWhenNicknameIsTooLong() {
        val exception = assertThrows<IllegalArgumentException> {
            User(
                id = UserId.of(1L),
                email = Email("test@example.com"),
                nickname = "this_nickname_is_way_too_long_for_validation"
            )
        }
        assertEquals("Nickname must be between 2 and 20 characters", exception.message)
    }

    @Test
    @DisplayName("OAuth 계정을 성공적으로 연결할 수 있다")
    fun shouldLinkOAuthAccountSuccessfully() {
        val user = createTestUser()
        val oauthAccount = createGoogleOAuthAccount()

        val updatedUser = user.linkOAuthAccount(oauthAccount)

        assertEquals(1, updatedUser.oauthAccounts.size)
        assertTrue(updatedUser.hasOAuthProvider(OAuthProvider.GOOGLE))
        assertEquals(oauthAccount, updatedUser.getOAuthAccount(OAuthProvider.GOOGLE))
    }

    @Test
    @DisplayName("동일한 제공자의 OAuth 계정은 중복 연결할 수 없다")
    fun shouldNotAllowDuplicateOAuthProvider() {
        val user = createTestUser()
        val oauthAccount1 = createGoogleOAuthAccount()
        val oauthAccount2 = OAuthAccount(
            provider = OAuthProvider.GOOGLE,
            oauthId = "google-789",
            email = "another@gmail.com"
        )

        val userWithAccount = user.linkOAuthAccount(oauthAccount1)

        val exception = assertThrows<IllegalStateException> {
            userWithAccount.linkOAuthAccount(oauthAccount2)
        }
        assertEquals("Google account is already linked", exception.message)
    }

    @Test
    @DisplayName("OAuth 계정을 성공적으로 연결 해제할 수 있다")
    fun shouldUnlinkOAuthAccountSuccessfully() {
        val user = createTestUser()
        val googleAccount = createGoogleOAuthAccount()
        val kakaoAccount = OAuthAccount(
            provider = OAuthProvider.KAKAO,
            oauthId = "kakao-123",
            email = "test@kakao.com"
        )

        val userWithAccounts = user
            .linkOAuthAccount(googleAccount)
            .linkOAuthAccount(kakaoAccount)

        val userAfterUnlink = userWithAccounts.unlinkOAuthAccount(OAuthProvider.GOOGLE)

        assertEquals(1, userAfterUnlink.oauthAccounts.size)
        assertFalse(userAfterUnlink.hasOAuthProvider(OAuthProvider.GOOGLE))
        assertTrue(userAfterUnlink.hasOAuthProvider(OAuthProvider.KAKAO))
    }

    @Test
    @DisplayName("존재하지 않는 OAuth 계정은 연결 해제할 수 없다")
    fun shouldNotUnlinkNonExistentOAuthAccount() {
        val user = createTestUser()

        val exception = assertThrows<IllegalStateException> {
            user.unlinkOAuthAccount(OAuthProvider.GOOGLE)
        }
        assertEquals("No Google account found to unlink", exception.message)
    }

    @Test
    @DisplayName("OAuth를 통해 신규 사용자를 생성할 수 있다")
    fun shouldCreateUserFromOAuth() {
        val email = Email("oauth@example.com")
        val nickname = "oauthuser"
        val oauthAccount = createGoogleOAuthAccount()

        val user = User.createFromOAuth(email, nickname, oauthAccount)

        assertEquals(email, user.email)
        assertEquals(nickname, user.nickname)
        assertEquals(1, user.oauthAccounts.size)
        assertTrue(user.hasOAuthProvider(OAuthProvider.GOOGLE))
        assertTrue(user.isNewUser())
    }

    @Test
    @DisplayName("특정 OAuth 제공자 계정을 보유하고 있는지 확인할 수 있다")
    fun shouldCheckIfUserHasOAuthProvider() {
        val user = createTestUser()
        val oauthAccount = createGoogleOAuthAccount()

        val userWithAccount = user.linkOAuthAccount(oauthAccount)

        assertTrue(userWithAccount.hasOAuthProvider(OAuthProvider.GOOGLE))
        assertFalse(userWithAccount.hasOAuthProvider(OAuthProvider.KAKAO))
        assertFalse(userWithAccount.hasOAuthProvider(OAuthProvider.NAVER))
    }
}
