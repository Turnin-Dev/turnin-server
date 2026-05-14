package com.turnin.domain.auth.domain.repository

import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.domain.auth.domain.model.Register

interface AuthRepository {
    /**
     * Provider(소셜로그인 플랫폼)와 Provider(소셜로그인 ID)로 [AuthUser]를 찾는다. (비활성화 사용자 제외)
     *
     * @param provider 소셜로그인 플랫폼
     * @param providerId 소셜로그인 ID
     *
     * @return [AuthUser] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findAuthUserByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser?

    /**
     * 사용자 ID를 통해 사용자를 조회한다. (비활성화 사용자 제외)
     *
     * @param userId 사용자 ID
     *
     * @return [AuthUser] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findUserByUserId(userId: UserId): AuthUser?

    /**
     * 사용자 표시 ID 존재 여부를 확인한다.
     *
     * 사용자 표시 ID를 찾는 즉시 중단 후 반환한다.
     *
     * @param displayId 사용자 표시 ID
     *
     * @return [Boolean] - 사용자를 찾았다면 `true`, 찾지 못했다면 `false`
     */
    suspend fun existsByDisplayId(displayId: DisplayId): Boolean

    /**
     * [Register]로 회원가입(저장)을 한다.
     *
     * 기본적으로 활성화된 사용자로 저장된다.
     *
     * - 활성화된 사용자: `role: Role.User`, `isActive: true`
     *
     * @exception com.turnin.common.db.DatabaseException.DuplicatedDataException - 이미 존재하는 사용자 저장 시 예외 발생
     */
    suspend fun save(register: Register): AuthUser

    /**
     * 사용자의 마지막 로그인 시점을 업데이트 한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun updateLastLoginAt(userId: UserId)
}
