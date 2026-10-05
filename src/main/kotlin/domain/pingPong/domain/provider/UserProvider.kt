package com.turnin.domain.pingPong.domain.provider

import com.turnin.common.model.UserName
import com.turnin.common.model.id.UserId

/**
 * 외부에서 제공받은 사용자 API
 */
interface UserProvider {
    /**
     * 사용자 이름을 조회한다.
     * (비활성화 사용자 제외)
     *
     * @param userId 사용자 ID
     *
     * @return 활성 사용자라면 [UserName]을 반환하고, 없거나 비활성화된 사용자라면 `null`을 반환한다.
     */
    suspend fun findUserName(userId: UserId): UserName?
}
