package com.turnin.domain.userKeyword.domain.provider

import com.turnin.common.model.id.UserKeywordId

/**
 * 외부에서 제공되는 신고 API
 */
interface ReportProvider {
    /**
     * 사용자 키워드 ID가 신고 내역에 존재하는지 확인한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     */
    suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean
}
