package com.peekr.domain.friend.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 친구 삭제 요청 바디
 *
 * @property requesterId 친구 삭제를 요청한 사용자 ID
 * @property receiverId 친구 삭제 대상 사용자 ID
 */
@Serializable
data class DeleteFriendRequest(
    val requesterId: Long,
    val receiverId: Long,
) {
    companion object {
        val sample = DeleteFriendRequest(
            requesterId = 1,
            receiverId = 2,
        )
    }
}
