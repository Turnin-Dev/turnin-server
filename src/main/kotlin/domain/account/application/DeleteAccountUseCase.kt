package com.turnin.domain.account.application

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.account.exception.AccountException
import com.turnin.domain.auth.application.provider.AuthDeletionSupportApi
import com.turnin.domain.block.application.provider.BlockDeletionSupportApi
import com.turnin.domain.file.application.provider.FileDeletionSupportApi
import com.turnin.domain.file.domain.model.FileCategory
import com.turnin.domain.friend.application.provider.FriendDeletionSupportApi
import com.turnin.domain.notification.application.provider.NotificationDeletionSupportApi
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi

/**
 * 계정 삭제
 *
 * @see invoke
 */
class DeleteAccountUseCase(
    private val authDeletionSupportApi: AuthDeletionSupportApi,
    private val userDeletionSupportApi: UserDeletionSupportApi,
    private val friendDeletionSupportApi: FriendDeletionSupportApi,
    private val blockDeletionSupportApi: BlockDeletionSupportApi,
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi,
    private val fileDeletionSupportApi: FileDeletionSupportApi,
    private val notificationDeletionSupportApi: NotificationDeletionSupportApi,
) {
    /**
     * 계정을 삭제한다.
     *
     * 자세한 내용은 기능 명세서 **`RQ-3`** 참고
     *
     * @param userId 사용자 ID
     *
     * @throws AccountException.UserNotFound 사용자가 존재하지 않는 경우 예외가 발생한다.
     */
    suspend operator fun invoke(userId: Long) {
        LOGGER.info(
            message = "Account deletion attempt: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.WITHDRAWAL_ATTEMPT.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )

        val profileImageUrl = suspendTransaction {
            val userIDVO = UserId(userId)
            val user = userDeletionSupportApi.findById(userIDVO)
                ?: throw AccountException.UserNotFound()

            // 1. 사용자 데이터 삭제/비식별화
            // - 피드/탐색 조회 쿼리 때문에 삭제 시 트랜잭션 내부에서 정확한 순서대로 삭제해야 한다.
            authDeletionSupportApi.deleteRefreshToken(userIDVO)
            notificationDeletionSupportApi.deleteAll(userIDVO)
            friendDeletionSupportApi.deleteAll(userIDVO)
            blockDeletionSupportApi.deleteAll(userIDVO)
            // TODO: 사용자, 키워드 비활성화 시 필요없는 부분은 전부 null혹은 빈 문자열로 바꾸는 것을 고려해야 함.
            userKeywordDeletionSupportApi.deactivateAll(userIDVO)
            userDeletionSupportApi.anonymizeProviderId(userIDVO, user.providerId)
            userDeletionSupportApi.deactivate(userIDVO)

            // 트랜잭션 외부에서 필요한 데이터 반환
            user.profileImageUrl
        }

        LOGGER.info(
            message = "Account deletion successful: userId=$userId",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.WITHDRAWAL_SUCCESS.value,
                LogTag.USER_ID.key to userId.toString(),
            ),
        )

        // 2. 파일 서버 정리 (스토리지 서버에 있는 사용자의 데이터를 모두 삭제)
        profileImageUrl?.let {
            try {
                fileDeletionSupportApi.deleteFile(it, FileCategory.PROFILE_IMAGE)
            } catch (e: Exception) {
                LOGGER.warn(
                    message = "Failed to delete profile image during account deletion.",
                    tags = mapOf(
                        LogTag.LOG_TYPE.key to LogType.NORMAL.value,
                        LogTag.ACTION.key to LogAction.FILE_DELETE_FAILURE.value,
                        LogTag.USER_ID.key to userId.toString(),
                    ),
                    e = e,
                )
            }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger<DeleteAccountUseCase>()
