package com.turnin.domain.account.application

import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.report.application.provider.ReportDeletionSupportApi
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi

/**
 * 계약 기간이 만료된 계정들을 Hard Delete 한다.
 *
 * @see invoke
 */
class HardDeleteExpiredAccountsUseCase(
    private val userDeleteSupportApi: UserDeletionSupportApi,
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi,
    private val reportDeletionSupportApi: ReportDeletionSupportApi,
) {
    /**
     * 사용자의 모든 데이터를 Hard Delete 한다.
     *
     * **이 유스케이스는 사용자의 모든 데이터를 삭제하므로 유의하여 사용해야 한다.**
     */
    suspend operator fun invoke(userId: Long) {
        LOGGER.info(
            message = "Account Hard Deletion attempt: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.HARD_DELETE_ATTEMPT.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )

        // DeleteAccountUseCase에서 Soft Delete로 처리된 작업 외의 작업만 처리한다.
        // AnnouncementRead(공지 읽음 여부) 테이블 같은 단순 매핑성 테이블은 CASCADE에 맡긴다.
        val userIdVO = UserId(userId)
        reportDeletionSupportApi.deleteByUserId(userIdVO)
        userKeywordDeletionSupportApi.deleteByUserId(userIdVO)
        userDeleteSupportApi.delete(userIdVO)

        LOGGER.info(
            message = "Account Hard Deletion successful: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.HARD_DELETE_SUCCESS.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<HardDeleteExpiredAccountsUseCase>()
