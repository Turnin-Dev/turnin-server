package com.turnin.domain.userKeyword.domain.provider

import com.turnin.common.model.id.UserKeywordId

/**
 * 외부에서 제공되는 핑퐁 API
 */
interface PingPongProvider {
    /**
     * 게시물(사용자 키워드)에 달린 핑퐁을 전부 삭제한다.
     *
     * @param userKeywordId 사용자 키워드(게시물) ID
     */
    suspend fun deleteAllByUserKeywordId(userKeywordId: UserKeywordId)
}
