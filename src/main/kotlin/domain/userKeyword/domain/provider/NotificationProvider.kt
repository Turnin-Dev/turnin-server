package com.turnin.domain.userKeyword.domain.provider

import com.turnin.common.model.NotificationType

/**
 * 외부에서 제공받은 알림 API
 */
interface NotificationProvider {
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
    )
}
