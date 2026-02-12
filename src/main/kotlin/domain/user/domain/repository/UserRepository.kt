package com.peekr.domain.user.domain.repository

import com.peekr.common.model.Introduce
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch

interface UserRepository {
    /**
     * 사용자 존재 여부 확인
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
     * ID를 통해 사용자를 조회한다. (차단된 사용자는 제외한다.)
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
     * ID 목록을 통해 사용자 목록을 조회한다.
     *
     * @param ids 사용자 ID 목록
     *
     * @return 사용자 목록
     */
    suspend fun findByIds(ids: List<UserId>): List<User>

    /**
     * 사용자 표시 ID를 통해 사용자를 조회한다.
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
}
