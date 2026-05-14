package com.turnin.domain.friend.domain.model

import com.turnin.common.model.FriendRequestStatus

/**
 * 기존 친구 관계
 *
 * @property status 친구 요청 상태
 * @property isReverse 역방향 여부
 */
data class ExistingRelation(
    val status: FriendRequestStatus,
    val isReverse: Boolean,
)

/**
 * 친구 요청 컨텍스트
 *
 * @property requesterInfo 요청자 정보
 * @property receiverInfo 수신자 정보
 * @property isBlocked 차단 여부
 * @property existingRelation 기존 친구 관계
 */
data class FriendRequestContext(
    val requesterInfo: UserInfo,
    val receiverInfo: UserInfo,
    val isBlocked: Boolean,
    val existingRelation: ExistingRelation?,
)
