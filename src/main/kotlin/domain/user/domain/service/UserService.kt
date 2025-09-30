package com.peekr.domain.user.domain.service

import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch

interface UserService {
    /**
     * ID를 통해 사용자를 조회한다.
     *
     * @param id 사용자 ID
     *
     * @return [User] - 사용자가 존재하지 않을 때는 **`null`** 반환
     */
    suspend fun getUserById(id: UserId): User?

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
    suspend fun updateUser(
        userId: UserId,
        patch: UserPatch,
    ): Boolean
}
