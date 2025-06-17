package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.application.port.`in`.LinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.`in`.RegisterWithOAuthCommand
import com.dd3ok.authservice.application.port.`in`.UnlinkOAuthAccountCommand
import com.dd3ok.authservice.application.port.`in`.UpdateUserCommand
import com.dd3ok.authservice.application.port.`in`.UserRegistrationResponse
import com.dd3ok.authservice.application.port.`in`.UserRegistrationUseCase
import com.dd3ok.authservice.application.port.`in`.UserResponse
import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.exception.UserNotFoundException
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.Email
import com.dd3ok.authservice.domain.vo.OAuthAccount
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class UserRegistrationService(
    private val userRepository: UserRepository
) : UserRegistrationUseCase {
    
    override fun registerWithOAuth(command: RegisterWithOAuthCommand): UserRegistrationResponse {
        val email = Email(command.email)
        val provider = OAuthProvider.fromClientName(command.provider)
        
        // 이미 존재하는 사용자인지 확인
        val existingUser = userRepository.findByOAuthAccount(provider, command.oauthId)
        if (existingUser != null) {
            return UserRegistrationResponse(
                userId = existingUser.id!!.value,
                email = existingUser.email.value,
                nickname = existingUser.nickname,
                isNewUser = false
            )
        }
        
        // 이메일로 기존 사용자 찾기 (다른 OAuth 계정으로 가입된 경우)
        val userByEmail = userRepository.findByEmail(email)
        if (userByEmail != null) {
            // 기존 사용자에 OAuth 계정 연결
            val oauthAccount = OAuthAccount(provider, command.oauthId, command.email)
            val updatedUser = userByEmail.linkOAuthAccount(oauthAccount)
            val savedUser = userRepository.save(updatedUser)
            
            return UserRegistrationResponse(
                userId = savedUser.id!!.value,
                email = savedUser.email.value,
                nickname = savedUser.nickname,
                isNewUser = false
            )
        }
        
        // 완전히 새로운 사용자 생성
        val oauthAccount = OAuthAccount(provider, command.oauthId, command.email)
        val newUser = User.createFromOAuth(email, command.nickname, oauthAccount)
        val savedUser = userRepository.save(newUser)
        
        return UserRegistrationResponse(
            userId = savedUser.id!!.value,
            email = savedUser.email.value,
            nickname = savedUser.nickname,
            isNewUser = true
        )
    }
    
    override fun linkOAuthAccount(command: LinkOAuthAccountCommand): UserResponse {
        val user = userRepository.findById(UserId.of(command.userId))
            ?: throw UserNotFoundException("User not found with id: ${command.userId}")
        
        val provider = OAuthProvider.fromClientName(command.provider)
        val oauthAccount = OAuthAccount(provider, command.oauthId, command.email)
        
        val updatedUser = user.linkOAuthAccount(oauthAccount)
        val savedUser = userRepository.save(updatedUser)
        
        return savedUser.toUserResponse()
    }
    
    override fun unlinkOAuthAccount(command: UnlinkOAuthAccountCommand): UserResponse {
        val user = userRepository.findById(UserId.of(command.userId))
            ?: throw UserNotFoundException("User not found with id: ${command.userId}")
        
        val provider = OAuthProvider.fromClientName(command.provider)
        val updatedUser = user.unlinkOAuthAccount(provider)
        val savedUser = userRepository.save(updatedUser)
        
        return savedUser.toUserResponse()
    }
    
    private fun User.toUserResponse(): UserResponse {
        return UserResponse(
            userId = this.id!!.value,
            email = this.email.value,
            nickname = this.nickname,
            linkedProviders = this.oauthAccounts.map { it.provider.displayName }
        )
    }

    @Transactional(readOnly = true)
    override fun getCurrentUser(userId: Long): UserResponse {
        val user = userRepository.findById(UserId.of(userId))
            ?: throw UserNotFoundException("User not found with id: $userId")

        return UserResponse(
            userId = user.id!!.value,
            email = user.email.value,
            nickname = user.nickname,
            linkedProviders = user.oauthAccounts.map { it.provider.displayName }
        )
    }

    @Transactional
    override fun updateUser(command: UpdateUserCommand): UserResponse {
        val user = userRepository.findById(UserId.of(command.userId))
            ?: throw UserNotFoundException("User not found with id: ${command.userId}")

        val updatedUser = user.copy(nickname = command.nickname)
        userRepository.save(updatedUser)

        return UserResponse(
            userId = updatedUser.id!!.value,
            email = updatedUser.email.value,
            nickname = updatedUser.nickname,
            linkedProviders = updatedUser.oauthAccounts.map { it.provider.displayName }
        )
    }
}
