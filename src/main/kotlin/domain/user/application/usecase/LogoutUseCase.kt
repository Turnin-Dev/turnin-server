package com.peekr.domain.user.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.util.log.AppLoggerFactory
import com.peekr.common.util.log.LogAction
import com.peekr.common.util.log.LogTag
import com.peekr.common.util.log.LogType
import com.peekr.domain.user.domain.provider.AuthProvider
import com.peekr.domain.user.domain.provider.NotificationProvider
import io.ktor.utils.io.CancellationException

/**
 * 로그아웃
 *
 * @see invoke
 */
class LogoutUseCase(
    private val authProvider: AuthProvider,
    private val notificationProvider: NotificationProvider,
) {
    /**
     * 로그아웃을 수행한다.
     *
     * 자세한 내용은 기능 명세서 **`RQ-2`** 참고
     *
     * @param userId 사용자 ID
     * @param token FCM 토큰
     */
    suspend operator fun invoke(
        userId: Long,
        token: String,
    ) {
        LOGGER.info(
            message = "Logout attempt: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.LOGOUT_ATTEMPT.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )

        val userIDVO = UserId(userId)

        // 1. 토큰 삭제
        authProvider.deleteRefreshToken(userIDVO)

        // 2. 기기 정보 해제 (사용자와 매핑된 FCM 토큰 정보 제거)
        // 부가 작업이므로 실패 시 계속 진행
        if (token.isNotEmpty()) {
            runCatching { notificationProvider.deactivate(userIDVO, token) }
                .onFailure { e ->
                    if (e is CancellationException) throw e

                    LOGGER.warn(
                        message = "Failed to deactivate notification for user $userId",
                        tags = mapOf(
                            LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                            LogTag.ACTION.key to LogAction.FCM_DEACTIVATE_FAILURE.value,
                        ),
                        e = e,
                    )
                }
        }

        LOGGER.info(
            message = "Logout successful: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.LOGOUT_SUCCESS.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<LogoutUseCase>()
