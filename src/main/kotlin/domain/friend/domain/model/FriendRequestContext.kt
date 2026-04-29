package com.turnin.domain.friend.domain.model

/**
 * 친구 요청 컨텍스트
 *
 * @property requesterInfo 요청자 정보
 * @property receiverInfo 수신자 정보
 * @property isBlocked 차단 여부
 */
data class FriendRequestContext(
    val requesterInfo: UserInfo,
    val receiverInfo: UserInfo,
    val isBlocked: Boolean,
)
