package com.turnin.common.batch

import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.account.application.HardDeleteExpiredAccountsUseCase
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration

/**
 * 계약 기간이 만료된 사용자 계정 삭제 (Hard Delete) 배치
 */
class HardDeleteExpiredAccountsBatch(
    private val hardDeleteExpiredAccountsUseCase: HardDeleteExpiredAccountsUseCase,
    private val userDeletionSupportApi: UserDeletionSupportApi,
) {
    suspend fun run() {
        LOGGER.info(
            message = "HardDeleteExpiredAccountsBatch started",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.DELETE_ACCOUNT_BATCH_STARTED.value,
            ),
        )

        var successCount = 0
        var failCount = 0

        // 탈퇴 기준 시각: 현재 시각으로부터 1년 전
        val expiredBefore = TurninDateTime.now().minus(365.days.toJavaDuration())

        var afterId: Long? = null
        while (true) {
            val expiredUsers = userDeletionSupportApi.findExpiredUsers(
                expiredBefore = expiredBefore,
                limit = CHUNK_SIZE,
                afterId = afterId,
            )

            if (expiredUsers.isEmpty()) break

            expiredUsers.forEach { userId ->
                runCatching {
                    hardDeleteExpiredAccountsUseCase(userId)
                }.onSuccess {
                    successCount++
                }.onFailure { e ->
                    failCount++
                    LOGGER.error(
                        message = "HardDeleteExpiredAccountsBatch failed: userId=$userId",
                        e = e,
                        tags = mapOf(
                            LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                            LogTag.ACTION.key to LogAction.DELETE_ACCOUNT_BATCH_ITEM_FAILED.value,
                            LogTag.USER_ID.key to userId.toString(),
                        ),
                    )
                }
                afterId = userId
            }
        }

        LOGGER.info(
            message = "HardDeleteExpiredAccountsBatch finished: success=$successCount, fail=$failCount",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.DELETE_ACCOUNT_BATCH_SUCCESS.value,
            ),
        )
    }

    companion object {
        /** 배치 1회 조회 시 처리할 최대 사용자 수 */
        const val CHUNK_SIZE = 100
    }
}

private val LOGGER = AppLoggerFactory.createLogger<HardDeleteExpiredAccountsBatch>()
