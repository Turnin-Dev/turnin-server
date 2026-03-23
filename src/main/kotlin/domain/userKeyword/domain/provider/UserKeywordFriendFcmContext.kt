package com.peekr.domain.userKeyword.domain.provider

/**
 * 새 키워드 알림 전송에 필요한 친구 FCM 컨텍스트
 *
 * friend 도메인의 `FriendFcmContext`를 userKeyword 도메인에 맞게 변환한 모델
 *
 * @property friendTokens 알림 수신 대상 친구들의 FCM 토큰 목록 (최대 500개)
 * @property senderName 키워드를 등록한 사용자 이름 (알림 메시지에 사용)
 */
data class UserKeywordFriendFcmContext(
    val friendTokens: List<String>,
    val senderName: String,
)
