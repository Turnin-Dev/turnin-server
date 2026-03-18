package com.peekr.domain.notification.application.usecase

data class NotificationUseCases(
    /**
     * FCM 토큰 등록
     * @see RegisterFcmTokenUseCase
     */
    val registerToken: RegisterFcmTokenUseCase,
    /**
     *
     * 특정 FCM 토큰 비활성화
     * @see DeactivateFcmTokenUseCase
     */
    val deactivateToken: DeactivateFcmTokenUseCase,
    /**
     * 모든 FCM 토큰 비활성화
     * @see DeactivateAllFcmTokensUseCase
     */
    val deactivateAllTokens: DeactivateAllFcmTokensUseCase,
    /**
     * 모든 FCM 토큰 삭제
     * @see DeleteAllFcmTokensUseCase
     */
    val deleteAllTokens: DeleteAllFcmTokensUseCase,
    /**
     * 특정 사용자에게 알림 전송 및 저장
     * @see SendNotificationUseCase
     */
    val sendNotification: SendNotificationUseCase,
    /**
     * 전체 사용자에게 브로드캐스트 알림 전송 및 저장
     * @see SendBroadcastUseCase
     */
    val sendBroadcast: SendBroadcastUseCase,
    /**
     * 알림 목록 조회
     * @see GetNotificationsUseCase
     */
    val getNotifications: GetNotificationsUseCase,
    /**
     * 특정 알림 읽음 처리
     * @see MarkAsReadUseCase
     */
    val markAsRead: MarkAsReadUseCase,
)
