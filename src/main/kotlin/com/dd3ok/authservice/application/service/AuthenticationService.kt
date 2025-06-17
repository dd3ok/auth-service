package com.dd3ok.authservice.application.service

import com.dd3ok.authservice.application.port.`in`.AuthenticationResponse
import com.dd3ok.authservice.application.port.`in`.AuthenticationUseCase
import com.dd3ok.authservice.application.port.`in`.LogoutCommand
import com.dd3ok.authservice.application.port.`in`.OAuthAuthenticationCommand
import com.dd3ok.authservice.application.port.`in`.RefreshTokenCommand
import com.dd3ok.authservice.application.port.`in`.RegisterWithOAuthCommand
import com.dd3ok.authservice.application.port.`in`.TokenResponse
import com.dd3ok.authservice.application.port.`in`.UserResponse
import com.dd3ok.authservice.application.port.out.OAuthClientPort
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.dd3ok.authservice.application.port.out.RefreshTokenRepository
import com.dd3ok.authservice.application.port.out.TokenPort
import com.dd3ok.authservice.application.port.out.UserRepository
import com.dd3ok.authservice.domain.exception.InvalidOAuthStateException
import com.dd3ok.authservice.domain.exception.UserNotFoundException
import com.dd3ok.authservice.domain.model.RefreshToken
import com.dd3ok.authservice.domain.model.User
import com.dd3ok.authservice.domain.vo.OAuthProvider
import com.dd3ok.authservice.domain.vo.UserId
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class AuthenticationService(
    private val userRepository: UserRepository,
    private val oauthClientPort: OAuthClientPort,
    private val tokenPort: TokenPort,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRegistrationService: UserRegistrationService
) : AuthenticationUseCase {

    override fun authenticateWithOAuth(command: OAuthAuthenticationCommand): AuthenticationResponse {
        val provider = OAuthProvider.fromClientName(command.provider)
        
        // 1. OAuth 제공자로부터 사용자 정보 가져오기
        val oauthUserInfo = getOAuthUserInfo(provider, command.authorizationCode, command.redirectUri, command.state)

        // 2. 기존 사용자 찾기 또는 새 사용자 등록
        val user = findOrCreateUser(provider, oauthUserInfo)
        
        // 3. 토큰 생성
        val tokens = generateTokens(user.id!!)
        
        return AuthenticationResponse(
            user = user.toUserResponse(),
            tokens = tokens
        )
    }
    
    override fun refreshToken(command: RefreshTokenCommand): TokenResponse {
        // 1. Refresh Token 검증
        val refreshToken = refreshTokenRepository.findByToken(command.refreshToken)
            ?: throw InvalidOAuthStateException("Invalid refresh token")
        
        if (!refreshToken.isValid()) {
            throw InvalidOAuthStateException("Refresh token expired or revoked")
        }
        
        // 2. 사용자 존재 확인
        val user = userRepository.findById(refreshToken.userId)
            ?: throw UserNotFoundException("User not found for refresh token")
        
        // 3. 기존 토큰 무효화 및 새 토큰 생성
        refreshTokenRepository.deleteByToken(command.refreshToken)
        
        return generateTokens(user.id!!)
    }
    
    override fun logout(command: LogoutCommand) {
        // 1. Access Token 무효화
        tokenPort.revokeToken(command.accessToken)
        
        // 2. 사용자의 모든 Refresh Token 무효화
        refreshTokenRepository.deleteByUserId(UserId.of(command.userId))
    }
    
    private fun getOAuthUserInfo(
        provider: OAuthProvider,
        authorizationCode: String,
        redirectUri: String?,
        state: String?
    ): OAuthUserInfoResponse {
        val tokenResponse = oauthClientPort.getAccessToken(provider, authorizationCode, redirectUri, state)
        return oauthClientPort.getUserInfo(provider, tokenResponse.accessToken)
    }
    
    private fun findOrCreateUser(provider: OAuthProvider, oauthUserInfo: OAuthUserInfoResponse): User {
        // 기존 OAuth 사용자 찾기
        val existingUser = userRepository.findByOAuthAccount(provider, oauthUserInfo.id)
        if (existingUser != null) {
            return existingUser
        }
        
        // 새 사용자 등록
        val registerCommand = RegisterWithOAuthCommand(
            provider = provider.clientName,
            oauthId = oauthUserInfo.id,
            email = oauthUserInfo.email,
            nickname = extractNickname(oauthUserInfo.name)
        )
        
        val registrationResponse = userRegistrationService.registerWithOAuth(registerCommand)
        
        return userRepository.findById(UserId.of(registrationResponse.userId))
            ?: throw IllegalStateException("Failed to retrieve registered user")
    }
    
    private fun generateTokens(userId: UserId): TokenResponse {
        val accessToken = tokenPort.generateAccessToken(userId)
        val refreshTokenValue = tokenPort.generateRefreshToken(userId)
        
        // Refresh Token을 저장소에 저장
        val refreshToken = RefreshToken(
            token = refreshTokenValue,
            userId = userId,
            expiresAt = LocalDateTime.now().plusDays(14) // 2주
        )
        refreshTokenRepository.save(refreshToken)
        
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            tokenType = "Bearer",
            expiresIn = 1800 // 30분 (seconds)
        )
    }
    
    private fun extractNickname(name: String): String {
        // 닉네임 길이 제한에 맞게 조정
        return if (name.length > 20) {
            name.substring(0, 20)
        } else if (name.length < 2) {
            "User${System.currentTimeMillis() % 10000}" // 기본 닉네임
        } else {
            name
        }
    }
    
    private fun User.toUserResponse(): UserResponse {
        return UserResponse(
            userId = this.id!!.value,
            email = this.email.value,
            nickname = this.nickname,
            linkedProviders = this.oauthAccounts.map { it.provider.displayName }
        )
    }
}