package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.application.port.`in`.LinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.`in`.RegisterWithOAuthCommand
import com.dd3ok.authservice.application.port.`in`.UnlinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.exception.UserNotFoundException
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("UserRegistrationService 테스트")
class UserRegistrationServiceTest {

    private val userRepository = mock<UserRepository>()
    private lateinit var userRegistrationService: UserRegistrationService

    @BeforeEach
    fun setUp() {
        clearInvocations(userRepository) // reset 대신 clearInvocations 사용
        userRegistrationService = UserRegistrationService(userRepository)
    }

    private fun createExistingUser(): User {
        val user = User(
            id = UserId.of(1L),
            email = Email("existing@example.com"),
            nickname = "existing"
        )
        return user.linkOAuthAccount(
            OAuthAccount(
                provider = OAuthProvider.GOOGLE,
                oauthId = "google-existing",
                email = "existing@gmail.com"
            )
        )
    }

    @Test
    @DisplayName("새로운 사용자를 OAuth로 등록할 수 있다")
    fun shouldRegisterNewUserWithOAuth() {
        // Given
        val command = RegisterWithOAuthCommand(
            provider = "google",
            oauthId = "google-123",
            email = "new@example.com",
            nickname = "newuser"
        )

        // Mock 설정
        doReturn(null).whenever(userRepository).findByOAuthAccount(OAuthProvider.GOOGLE, "google-123")
        doReturn(null).whenever(userRepository).findByEmail(Email("new@example.com"))
        doAnswer { invocation ->
            val user = invocation.getArgument<User>(0)
            user.withId(UserId.of(100L))
        }.whenever(userRepository).save(any())

        // When
        val response = userRegistrationService.registerWithOAuth(command)

        // Then
        assertTrue(response.isNewUser)
        assertEquals(100L, response.userId)
        assertEquals("new@example.com", response.email)
        assertEquals("newuser", response.nickname)

        verify(userRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("기존 OAuth 사용자는 새로 생성하지 않고 반환한다")
    fun shouldReturnExistingOAuthUser() {
        // Given
        val command = RegisterWithOAuthCommand(
            provider = "google",
            oauthId = "google-existing",
            email = "existing@example.com",
            nickname = "existing"
        )

        val existingUser = createExistingUser()
        doReturn(existingUser).whenever(userRepository).findByOAuthAccount(OAuthProvider.GOOGLE, "google-existing")

        // When
        val response = userRegistrationService.registerWithOAuth(command)

        // Then
        assertFalse(response.isNewUser)
        assertEquals(1L, response.userId)
        assertEquals("existing@example.com", response.email)

        verify(userRepository, never()).save(any())
    }

    @Test
    @DisplayName("기존 이메일 사용자에게 OAuth 계정을 연결할 수 있다")
    fun shouldLinkOAuthToExistingEmailUser() {
        // Given
        val command = RegisterWithOAuthCommand(
            provider = "kakao",
            oauthId = "kakao-123",
            email = "existing@example.com",
            nickname = "existing"
        )

        val existingUser = createExistingUser()
        doReturn(null).whenever(userRepository).findByOAuthAccount(OAuthProvider.KAKAO, "kakao-123")
        doReturn(existingUser).whenever(userRepository).findByEmail(Email("existing@example.com"))
        doAnswer { invocation ->
            invocation.getArgument<User>(0) // 그대로 반환
        }.whenever(userRepository).save(any())

        // When
        val response = userRegistrationService.registerWithOAuth(command)

        // Then
        assertFalse(response.isNewUser)
        assertEquals(1L, response.userId)

        verify(userRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("OAuth 계정을 연결할 수 있다")
    fun shouldLinkOAuthAccount() {
        // Given
        val command = LinkOAuthAccountCommand(
            userId = 1L,
            provider = "kakao",
            oauthId = "kakao-123",
            email = "test@kakao.com"
        )

        val existingUser = createExistingUser()

        // Mock 설정 - 명확하게 설정
        doReturn(existingUser).whenever(userRepository).findById(UserId.of(1L))
        doAnswer { invocation ->
            invocation.getArgument<User>(0)
        }.whenever(userRepository).save(any())

        // When
        val response = userRegistrationService.linkOAuthAccount(command)

        // Then
        assertEquals(1L, response.userId)
        assertTrue(response.linkedProviders.contains("Kakao"))

        verify(userRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 OAuth 계정을 연결하면 예외가 발생한다")
    fun shouldThrowExceptionWhenLinkingToNonExistentUser() {
        // Given
        val command = LinkOAuthAccountCommand(
            userId = 999L,
            provider = "kakao",
            oauthId = "kakao-123",
            email = "test@kakao.com"
        )

        doReturn(null).whenever(userRepository).findById(UserId.of(999L))

        // When & Then
        assertThrows<UserNotFoundException> {
            userRegistrationService.linkOAuthAccount(command)
        }

        verify(userRepository, never()).save(any())
    }

    @Test
    @DisplayName("OAuth 계정 연결을 해제할 수 있다")
    fun shouldUnlinkOAuthAccount() {
        // Given
        val command = UnlinkOAuthAccountCommand(
            userId = 1L,
            provider = "google"
        )

        // 여러 OAuth 계정을 가진 사용자 생성
        val userWithMultipleAccounts = createExistingUser().linkOAuthAccount(
            OAuthAccount(
                provider = OAuthProvider.KAKAO,
                oauthId = "kakao-123",
                email = "test@kakao.com"
            )
        )

        // Mock 설정
        doReturn(userWithMultipleAccounts).whenever(userRepository).findById(UserId.of(1L))
        doAnswer { invocation ->
            invocation.getArgument<User>(0)
        }.whenever(userRepository).save(any())

        // When
        val response = userRegistrationService.unlinkOAuthAccount(command)

        // Then
        assertEquals(1L, response.userId)
        assertFalse(response.linkedProviders.contains("Google"))
        assertTrue(response.linkedProviders.contains("Kakao"))

        verify(userRepository, times(1)).save(any())
    }
}
