package com.peekr.domain.notification.domain.model

import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.exception.NotificationErrorCode

/**
 * 알림 저장 요청용 모델
 *
 * @property userId 사용자 ID (수신자)
 * @property notiType 알림 유형
 * @property title 알림 제목
 * @property message 알림 본문
 * @property imageUrl 알림 첨부 이미지 URL
 * @property isBroadcast 브로드캐스트 여부
 * @property refId 참조 ID (발신자, 딥링크 용)
 * @property refType 참조 타입
 */
@ConsistentCopyVisibility
data class NotificationCommand private constructor(
    val userId: UserId?,
    val notiType: NotificationType,
    val title: String?,
    val message: String,
    val imageUrl: String? = null,
    val isBroadcast: Boolean = false,
    val refId: Long? = null,
    val refType: String? = null,
) {
    companion object {
        /**
         * 개인 알림 생성
         * @see [NotificationCommand]
         */
        fun personal(
            userId: UserId,
            notiType: NotificationType,
            title: String?,
            message: String,
            imageUrl: String? = null,
            refId: Long? = null,
            refType: String? = null,
        ): NotificationCommand {
            require(!notiType.isBroadcast) {
                NotificationErrorCode.InvalidPersonalNotificationType.description
            }
            return NotificationCommand(
                userId = userId,
                notiType = notiType,
                title = title,
                message = message,
                imageUrl = imageUrl,
                isBroadcast = false,
                refId = refId,
                refType = refType,
            )
        }

        /**
         * 브로드캐스트 알림 생성
         * @see [NotificationCommand]
         */
        fun broadcast(
            notiType: NotificationType,
            title: String?,
            message: String,
            refId: Long? = null,
            refType: String? = null,
        ): NotificationCommand {
            require(notiType.isBroadcast) {
                NotificationErrorCode.InvalidBroadcastNotificationType.description
            }
            return NotificationCommand(
                userId = null,
                notiType = notiType,
                title = title,
                message = message,
                isBroadcast = true,
                refId = refId,
                refType = refType,
            )
        }
    }
}
