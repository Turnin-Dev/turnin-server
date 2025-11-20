package com.peekr.domain.friend.application.dto

import com.peekr.common.model.FriendStatus
import com.peekr.domain.friend.domain.model.Friend

/**
 * 친구 DTO
 *
 * @property id 친구 ID
 * @property requesterId 요청한 사용자 ID
 * @property receiverId 요청 받은 사용자 ID
 * @property status 요청 상태
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
data class FriendDto(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: FriendStatus,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Friend.toDto(): FriendDto =
    FriendDto(
        id = id.value,
        requesterId = requesterId.value,
        receiverId = receiverId.value,
        status = status,
        respondedAt = respondedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
