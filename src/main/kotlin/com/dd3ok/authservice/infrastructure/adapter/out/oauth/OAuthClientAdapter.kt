package com.dd3ok.authservice.infrastructure.adapter.out.oauth

import com.dd3ok.authservice.application.port.out.OAuthClientPort
import com.dd3ok.authservice.application.port.out.OAuthTokenResponse
import com.dd3ok.authservice.application.port.out.OAuthUserInfoResponse
import com.dd3ok.authservice.domain.vo.OAuthProvider
import org.springframework.stereotype.Component

@Component
class OAuthClientAdapter(
    private val googleOAuthClient: GoogleOAuthClient,
    private val kakaoOAuthClient: KakaoOAuthClient,
    private val naverOAuthClient: NaverOAuthClient
) : OAuthClientPort {

    override fun getAccessToken(
        provider: OAuthProvider,
        authorizationCode: String,
        redirectUri: String?,
        state: String?
    ): OAuthTokenResponse {
        return when (provider) {
            OAuthProvider.GOOGLE -> googleOAuthClient.getAccessToken(authorizationCode, redirectUri)
            OAuthProvider.KAKAO -> kakaoOAuthClient.getAccessToken(authorizationCode, redirectUri)
            OAuthProvider.NAVER -> {
                if (state == null) throw IllegalArgumentException("Naver OAuth requires a state parameter.")
                naverOAuthClient.getAccessToken(authorizationCode, state, redirectUri)
            }
        }
    }

    override fun getUserInfo(provider: OAuthProvider, accessToken: String): OAuthUserInfoResponse {
        return when (provider) {
            OAuthProvider.GOOGLE -> googleOAuthClient.getUserInfo(accessToken)
            OAuthProvider.KAKAO -> kakaoOAuthClient.getUserInfo(accessToken)
            OAuthProvider.NAVER -> naverOAuthClient.getUserInfo(accessToken)
        }
    }
}
