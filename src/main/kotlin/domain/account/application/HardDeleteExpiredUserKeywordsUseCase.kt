package com.turnin.domain.account.application

import com.turnin.common.db.suspendTransaction
import com.turnin.domain.report.application.provider.ReportDeletionSupportApi
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi

/**
 * 삭제 보관 기간이 만료된 사용자 키워드(게시물)들을 Hard Delete 한다.
 *
 * @see invoke
 */
class HardDeleteExpiredUserKeywordsUseCase(
    private val reportDeletionSupportApi: ReportDeletionSupportApi,
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi,
) {
    /**
     * 사용자 키워드와 해당 키워드의 신고 내역을 Hard Delete 한다.
     *
     * 신고 FK(`report.reported_user_keyword_id`)가 RESTRICT 이므로 신고 내역을 먼저 삭제한다.
     * (핑퐁은 키워드 삭제 시점에 이미 삭제되었다)
     *
     * **이 유스케이스는 키워드와 신고 내역을 삭제하므로 유의하여 사용해야 한다.**
     *
     * @param userKeywordIds 삭제할 사용자 키워드 ID 목록
     */
    suspend operator fun invoke(userKeywordIds: List<Long>) {
        if (userKeywordIds.isEmpty()) return

        suspendTransaction {
            reportDeletionSupportApi.deleteByUserKeywordIds(userKeywordIds)
            userKeywordDeletionSupportApi.deleteByIds(userKeywordIds)
        }
    }
}
