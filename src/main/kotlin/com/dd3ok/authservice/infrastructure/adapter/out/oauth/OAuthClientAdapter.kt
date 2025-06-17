package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import com.dd3ok.authservice.application.port.out.OAuthClientPort
import com.dd3ok.authservice.application.port.out.OAuthTokenResponse
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.dd3ok.authservice.domain.vo.OAuthProvider
import org.springframework.stereotype.Component


@Component
class OAuthClientAdapter(
    private val googleOAuthClient: GoogleOAuthClient
) : OAuthClientPort {
    
    override fun getAccessToken(
        provider: OAuthProvider,
        authorizationCode: String,
        redirectUri: String?
    ): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.GOOGLE -> googleOAuthClient.getAccessToken(authorizationCode, redirectUri)
            OAuthProvider.KAKAO -> throw NotImplementedError("Kakao OAuth not implemented yet")
            OAuthProvider.NAVER -> throw NotImplementedError("Naver OAuth not implemented yet")
        }
    }
    
    override fun getUserInfo(provider: OAuthProvider, accessToken: String): OAuthUserInfoResponse {
        return when (provider) {
            OAuthProvider.GOOGLE -> googleOAuthClient.getUserInfo(accessToken)
            OAuthProvider.KAKAO -> throw NotImplementedError("Kakao OAuth not implemented yet")
            OAuthProvider.NAVER -> throw NotImplementedError("Naver OAuth not implemented yet")
        }
    }
}
