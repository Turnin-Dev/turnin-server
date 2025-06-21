package com.peekr.domain.auth.domain.repository

import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.exception.AuthException

interface AuthRepository {
    /**
     * Provider(소셜로그인 플랫폼)와 Provider(소셜로그인 ID)로 [AuthUser]를 찾는다.
     *
     * @param provider 소셜로그인 플랫폼 [SocialLoginProvider]
     * @param providerId 소셜로그인 ID
     */
    suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser?

    /**
     * [AuthUser]로 회원가입(저장)을 한다.
     *
     * @exception AuthException.DuplicateUserException - 이미 존재하는 사용자 저장 시 예외 발생
     */
    suspend fun save(authUser: AuthUser): AuthUser
}
