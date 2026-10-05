package com.turnin.domain.notification.application.provider

import com.turnin.common.firebase.FcmDataKey
import com.turnin.common.firebase.FcmMessage
import com.turnin.common.firebase.FcmService
import com.turnin.common.firebase.RefDataKey
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.application.usecase.SendNotificationUseCase
import com.turnin.domain.notification.domain.model.NotificationCommand
import com.turnin.domain.notification.domain.repository.FcmTokenRepository

/**
 * 외부에 제공할 알림 API
 */
class NotificationProviderApi(
    private val sendNotification: SendNotificationUseCase,
    private val fcmService: FcmService,
    private val fcmTokenRepository: FcmTokenRepository,
) {
    /**
     * 외부 기능 모듈에서 알림을 전송할 때 사용하는 단일 창구
     *
     * @param receiverId 수신자 ID
     * @param notiType 알림 유형
     * @param title 알림 제목
     * @param message 알림 본문
     * @param refId 딥링크용 참조 ID
     * @param refType 딥링크용 참조 타입
     * @param refData 딥링크용 부가 데이터 (알림 내역과 FCM data에 함께 담긴다)
     */
    suspend fun sendNotification(
        receiverId: UserId,
        notiType: NotificationType,
        title: String,
        message: String,
        refId: Long? = null,
        refType: String? = null,
        refData: Map<RefDataKey, String>? = null,
    ) {
        sendNotification.invoke(
            NotificationCommand.personal(
                receiverId = receiverId,
                notiType = notiType,
                title = title,
                message = message,
                refId = refId,
                refType = refType,
                refData = refData,
            ),
        )
    }

    /**
     * 여러 FCM 토큰으로 알림을 전송한다. (알림 내역 저장 없이 FCM 전송만)
     *
     * NEW_KEYWORD 알림처럼 알림 내역 저장이 불필요한 경우 사용한다.
     *
     * @param tokens FCM 토큰 목록
     * @param notiType 알림 유형
     * @param title 알림 제목
     * @param message 알림 본문
     * @param refId 딥링크용 참조 ID
     * @param refType 딥링크용 참조 타입
     * @param senderUserId 발신자 사용자 ID (딥링크용)
     */
    suspend fun sendNotificationToTokens(
        tokens: List<String>,
        notiType: NotificationType,
        title: String,
        message: String,
        refId: Long? = null,
        refType: String? = null,
        senderUserId: Long? = null,
    ) {
        if (tokens.isEmpty()) return

        fcmService.sendToUsers(
            tokens = tokens,
            message = FcmMessage(
                title = title,
                body = message,
                notiType = notiType,
                data = buildMap {
                    put(FcmDataKey.NOTI_TYPE, notiType.name)
                    refType?.let { put(FcmDataKey.REF_TYPE, it) }
                    refId?.let { put(FcmDataKey.REF_ID, it.toString()) }
                    senderUserId?.let {
                        put(RefDataKey.REF_OWNER_ID.key, it.toString())
                        // 구버전 앱 하위 호환: 최소 지원 앱 버전이 REF_OWNER_ID를 읽는 버전 이상이 되면 제거한다.
                        @Suppress("DEPRECATION")
                        put(FcmDataKey.USER_ID, it.toString())
                    }
                },
            ),
        )
    }

    /**
     * 특정 FCM 토큰을 비활성화한다. (로그아웃 시 호출)
     *
     * @param userId 토큰 소유자 ID
     * @param token 비활성화할 FCM 토큰
     */
    suspend fun deactivate(userId: UserId, token: String) =
        fcmTokenRepository.deactivate(userId, token)
}
