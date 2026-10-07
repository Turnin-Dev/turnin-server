package com.turnin.common.batch

import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.common.util.toKstDate
import com.turnin.domain.account.application.HardDeleteExpiredUserKeywordsUseCase
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

/**
 * 삭제 보관 기간(1년)이 만료된 사용자 키워드(게시물) 파기 (Hard Delete) 배치
 *
 * 작성자가 삭제했거나 계정 탈퇴로 삭제된 게시물 중 신고 내역 때문에 남겨둔 게시물을 파기한다.
 */
class HardDeleteExpiredUserKeywordsBatch(
    private val hardDeleteExpiredUserKeywordsUseCase: HardDeleteExpiredUserKeywordsUseCase,
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi,
) {
    suspend fun run() {
        val today = TurninDateTime.now().toKstDate()
        LOGGER.info(
            message = "HardDeleteExpiredUserKeywordsBatch running: date=$today",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.DELETE_USER_KEYWORD_BATCH_STARTED.value,
            ),
        )

        var successCount = 0
        var failCount = 0

        // 삭제 기준 시각: 현재 시각으로부터 1년 전
        val deletedBefore = TurninDateTime.now().minus(365.days.toJavaDuration())

        var afterId: Long? = null
        while (true) {
            val expiredUserKeywordIds = userKeywordDeletionSupportApi.findIdsDeletedBefore(
                deletedBefore = deletedBefore,
                limit = CHUNK_SIZE,
                afterId = afterId,
            )

            if (expiredUserKeywordIds.isEmpty()) break

            runCatching {
                hardDeleteExpiredUserKeywordsUseCase(expiredUserKeywordIds)
            }.onSuccess {
                successCount += expiredUserKeywordIds.size
            }.onFailure { e ->
                failCount += expiredUserKeywordIds.size
                LOGGER.error(
                    message = "HardDeleteExpiredUserKeywordsBatch failed: userKeywordIds=$expiredUserKeywordIds",
                    e = e,
                    tags = mapOf(
                        LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                        LogTag.ACTION.key to LogAction.DELETE_USER_KEYWORD_BATCH_ITEM_FAILED.value,
                    ),
                )
            }
            afterId = expiredUserKeywordIds.last()
        }

        val logMessage = if (failCount > 0) {
            "HardDeleteExpiredUserKeywordsBatch completed with some failures: success=$successCount, fail=$failCount"
        } else {
            "HardDeleteExpiredUserKeywordsBatch completed successfully: success=$successCount, fail=$failCount"
        }

        LOGGER.info(
            message = logMessage,
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.DELETE_USER_KEYWORD_BATCH_SUCCESS.value,
            ),
        )
    }

    companion object {
        /** 배치 1회 조회 시 처리할 최대 사용자 키워드 수 */
        const val CHUNK_SIZE = 100
    }
}

private val LOGGER = AppLoggerFactory.createLogger<HardDeleteExpiredUserKeywordsBatch>()
