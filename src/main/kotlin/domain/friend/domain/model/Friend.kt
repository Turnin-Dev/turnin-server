package com.peekr.domain.friend.domain.model

import com.peekr.common.model.FriendId
import com.peekr.common.model.FriendStatus
import com.peekr.common.model.UserId

/**
 * 친구 엔티티 모델
 *
 * @property id 친구 ID
 * @property requesterId 요청한 사용자 ID
 * @property receiverId 요청 받은 사용자 ID
 * @property status 요청 상태
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
data class Friend(
    val id: FriendId,
    val requesterId: UserId,
    val receiverId: UserId,
    val status: FriendStatus,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
