package com.turnin.domain.friend.domain.model

import com.turnin.common.model.FriendRequestStatus
import com.turnin.common.model.id.FriendId
import com.turnin.common.model.id.UserId

/**
 * 친구 엔티티 모델
 *
 * @property id 친구 ID
 * @property requesterId 요청한 사용자 ID
 * @property receiverId 요청 받은 사용자 ID
 * @property requestStatus 요청 상태
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
data class Friend(
    val id: FriendId,
    val requesterId: UserId,
    val receiverId: UserId,
    val requestStatus: FriendRequestStatus,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
