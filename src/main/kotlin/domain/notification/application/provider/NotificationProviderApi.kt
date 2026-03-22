package com.peekr.domain.notification.application.provider

import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.application.usecase.SendNotificationUseCase
import com.peekr.domain.notification.domain.model.NotificationCommand

/**
 * 외부에 제공할 알림 API
 */
class NotificationProviderApi(private val sendNotification: SendNotificationUseCase) {
    /**
     * 외부 기능 모듈에서 알림을 전송할 때 사용하는 단일 창구
     *
     * @param userId 수신자 ID
     * @param notiType 알림 유형
     * @param title 알림 제목
     * @param message 알림 본문
     * @param refId 딥링크용 참조 ID
     * @param refType 딥링크용 참조 타입
     */
    suspend fun sendNotification(
        userId: UserId,
        notiType: NotificationType,
        title: String,
        message: String,
        refId: Long? = null,
        refType: String? = null,
    ) {
        sendNotification.invoke(
            NotificationCommand.personal(
                userId = userId,
                notiType = notiType,
                title = title,
                message = message,
                refId = refId,
                refType = refType,
            ),
        )
    }
}
