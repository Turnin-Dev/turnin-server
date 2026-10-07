package com.turnin.domain.pingPong.domain.provider

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId

/**
 * 외부에서 제공받은 사용자 키워드 API
 */
interface UserKeywordProvider {
    /**
     * 사용자 키워드(게시물) 작성자 ID를 조회한다.
     * (차단된 사용자, 비활성화 사용자/사용자 키워드 제외)
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return 조회할 수 있는 사용자 키워드라면 작성자 ID를 반환하고, 아니라면 `null`을 반환한다.
     */
    suspend fun findOwnerId(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
    ): UserId?
}
