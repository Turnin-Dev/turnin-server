package com.peekr.domain.auth.domain.repository

import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.exception.AuthException

interface AuthRepository {
    suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser?

    /** @exception AuthException.DuplicateUserException - 이미 존재하는 사용자 저장 시 예외 발생 */
    suspend fun save(authUser: AuthUser): AuthUser
}
