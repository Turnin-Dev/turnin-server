package com.peekr.domain.friend.domain.model

/**
 * 친구들의 FCM 알림 전송에 필요한 컨텍스트
 *
 * @property friendTokens 알림 수신 대상 친구들의 FCM 토큰 목록 (최대 500개, 친구당 가장 최근 토큰 1개)
 * @property senderName 알림 발신자 이름 (알림 메시지에 사용)
 */
data class FriendFcmContext(
    val friendTokens: List<String>,
    val senderName: String,
) {
    companion object {
        const val MAX_NOTIFICATION_RECIPIENTS: Int = 500
    }
}
