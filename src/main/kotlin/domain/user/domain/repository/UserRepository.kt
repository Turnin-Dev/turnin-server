package com.peekr.domain.user.domain.repository

import com.peekr.common.model.Introduce
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch

interface UserRepository {
    /**
     * 사용자 존재 여부 확인 (비활성화 사용자 제외)
     *
     * @param id 사용자 ID
     */
    suspend fun existsUser(id: UserId): Boolean

    /**
     * ID를 통해 사용자를 조회한다.
     *
     * @param id 조회할 사용자 ID
     *
     * @return [User] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findById(id: UserId): User?

    /**
     * ID를 통해 사용자를 조회한다. (비활성화 사용자 제외)
     *
     * @param id 조회할 사용자 ID
     *
     * @return [User] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findActiveById(id: UserId): User?

    /**
     * ID를 통해 사용자를 조회한다. (차단된 사용자, 비활성화 사용자 제외)
     *
     * 1. 내가 차단한 사용자를 조회 시: isBlocked가 true인 채로 반환
     * 2. 내가 차단 당한 사용자를 조회 시: `null` 반환
     * 3. 상호 차단인 경우 사용자 조회 시: `null` 반환
     *
     * @param currentId 현재 조회를 요청한 사용자 ID
     * @param id 조회할 사용자 ID
     *
     * @return [User] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findVisibleById(
        currentId: UserId,
        id: UserId,
    ): User?

    /**
     * ID 목록을 통해 사용자 목록을 조회한다. (비활성화 사용자 제외)
     *
     * @param ids 사용자 ID 목록
     *
     * @return 사용자 목록
     */
    suspend fun findByIds(ids: List<UserId>): List<User>

    /**
     * 사용자 표시 ID를 통해 사용자를 조회한다. (비활성화 사용자 제외)
     *
     * @param id 사용자 표시 ID
     *
     * @return [User] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findByDisplayId(id: DisplayId): User?

    /**
     * 사용자 정보를 수정한다.
     *
     * 수정 가능한 정보
     * - 사용자 표시 ID
     * - 사용자 이름
     * - 사용자 프로필 이미지 url
     * - 사용자 소개 글
     *
     * @param userId 사용자 ID
     * @param patch [UserPatch]
     *
     * @return [Boolean] 업데이트 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun update(
        userId: UserId,
        patch: UserPatch,
    ): Boolean

    /**
     * 사용자 소개글을 수정한다.
     *
     * @param userId 사용자 ID
     * @param introduce 소개글
     *
     * @return [Boolean] 업데이트 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun updateIntroduce(
        userId: UserId,
        introduce: Introduce,
    ): Boolean

    /**
     * 탈퇴한 사용자의 providerId를 비식별화한다.
     *
     * 소셜 로그인은 provider + providerId 조합으로 사용자를 식별하기 때문에
     * 탈퇴 후 동일한 소셜 계정으로 재가입이 가능하도록 기존 providerId를 변조한다.
     *
     * (단, 탈퇴 후 동일 소셜 계정으로의 재가입은 서비스 초기 단계에서만 허용한다.)
     *
     * 변조 형식: `DELETED_(타임스탬프)_(providerId)`
     *
     * @param userId 비식별화할 사용자 ID
     * @param providerId 현재 providerId
     *
     * @return 업데이트 성공 여부
     */
    suspend fun anonymizeProviderId(
        userId: UserId,
        providerId: String,
    ): Boolean

    /**
     * 사용자를 비활성화한다.
     *
     * ###### 해당 메서드는 [userId]를 비활성화 상태로 변경하므로 주의해서 사용해야 한다.
     * ###### 비활성화된 사용자는 시스템에서 조회되지 않으며, 관련 데이터는 별도의 정리 작업을 통해 처리된다.
     *
     * @param userId 비활성화할 사용자 ID
     */
    suspend fun deactivate(userId: UserId)
}
