package com.peekr.domain.friend.domain.model

import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.FriendId
import com.peekr.common.model.id.UserId

/**
 * 받은 친구 요청자
 *
 * @property id 친구 ID
 * @property requesterId 요청한 사용자 ID
 * @property requestStatus 요청 상태
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
data class IncomingRequester(
    val id: FriendId,
    val requesterId: UserId,
    val requestStatus: FriendRequestStatus,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
