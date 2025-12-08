package com.peekr.domain.friend.presentation.dto

import com.peekr.common.model.FriendRequestStatus
import com.peekr.domain.friend.application.dto.FriendDto
import kotlinx.serialization.Serializable

/**
 * 친구 응답 바디
 *
 * @property id 친구 ID
 * @property requesterId 요청한 사용자 ID
 * @property receiverId 요청 받은 사용자 ID
 * @property status 요청 상태
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
@Serializable
data class FriendResponse(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: FriendRequestStatus,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = FriendResponse(
            id = 1,
            requesterId = 1,
            receiverId = 2,
            status = FriendRequestStatus.PENDING,
            respondedAt = null,
            createdAt = 1682870400000,
            updatedAt = 1682870400000,
        )
    }
}

fun FriendDto.toResponse(): FriendResponse =
    FriendResponse(
        id = id,
        requesterId = requesterId,
        receiverId = receiverId,
        status = requestStatus,
        respondedAt = respondedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
