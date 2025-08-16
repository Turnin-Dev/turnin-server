package com.peekr.domain.auth.domain.repository

import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.exception.AuthException

interface AuthRepository {
    /**
     * Provider(소셜로그인 플랫폼)와 Provider(소셜로그인 ID)로 [AuthUser]를 찾는다.
     *
     * @param provider 소셜로그인 플랫폼
     * @param providerId 소셜로그인 ID
     *
     * @return [AuthUser] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findAuthUserByProviderAndProviderId(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): AuthUser?

    /**
     * 사용자 ID를 통해 사용자를 조회한다.
     *
     * @param userId 사용자 ID
     *
     * @return [AuthUser] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findUserByUserId(userId: Long): AuthUser?

    /**
     * [Register]로 회원가입(저장)을 한다.
     *
     * 기본적으로 활성화된 사용자로 저장된다.
     *
     * - 활성화된 사용자: `role: Role.User`, `isActive: true`
     *
     * @exception AuthException.DuplicateUserException - 이미 존재하는 사용자 저장 시 예외 발생
     */
    suspend fun save(register: Register): AuthUser
}
