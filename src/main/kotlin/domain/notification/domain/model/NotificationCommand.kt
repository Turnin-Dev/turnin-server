package com.turnin.domain.notification.domain.model

import com.turnin.common.firebase.RefDataKey
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.exception.NotificationErrorCode

/**
 * 알림 저장 요청용 모델
 *
 * @property receiverId 수신자 ID (브로드캐스트 알림은 null)
 * @property notiType 알림 유형
 * @property title 알림 제목
 * @property message 알림 본문
 * @property imageUrl 알림 첨부 이미지 URL
 * @property isBroadcast 브로드캐스트 여부
 * @property refId 참조 ID (발신자, 딥링크 용)
 * @property refType 참조 타입
 * @property refData 딥링크용 부가 데이터 (refId/refType 외에 화면 이동에 필요한 값, 알림 내역과 FCM data에 같은 키로 담긴다)
 */
@ConsistentCopyVisibility
data class NotificationCommand private constructor(
    val receiverId: UserId?,
    val notiType: NotificationType,
    val title: String?,
    val message: String,
    val imageUrl: String? = null,
    val isBroadcast: Boolean = false,
    val refId: Long? = null,
    val refType: String? = null,
    val refData: Map<RefDataKey, String>? = null,
) {
    companion object {
        /**
         * 개인 알림 생성
         * @see [NotificationCommand]
         */
        fun personal(
            receiverId: UserId,
            notiType: NotificationType,
            title: String?,
            message: String,
            imageUrl: String? = null,
            refId: Long? = null,
            refType: String? = null,
            refData: Map<RefDataKey, String>? = null,
        ): NotificationCommand {
            require(!notiType.isBroadcast) {
                NotificationErrorCode.InvalidPersonalNotificationType.description
            }
            return NotificationCommand(
                receiverId = receiverId,
                notiType = notiType,
                title = title,
                message = message,
                imageUrl = imageUrl,
                isBroadcast = false,
                refId = refId,
                refType = refType,
                refData = refData,
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
                receiverId = null,
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
