package com.peekr.domain.friend.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 친구 요청 바디
 *
 * @property requesterId 친구 요청을 한 사용자 ID
 * @property receiverId 친구 요청을 벋을 사용자 ID
 */
@Serializable
data class AddFriendRequest(
    val requesterId: Long,
    val receiverId: Long,
) {
    companion object {
        val sample = AddFriendRequest(
            requesterId = 1,
            receiverId = 2,
        )
    }
}
