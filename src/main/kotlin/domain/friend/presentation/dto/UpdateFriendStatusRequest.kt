package com.peekr.domain.friend.presentation.dto

import com.peekr.common.model.FriendStatus
import kotlinx.serialization.Serializable

/**
 * 친구 상태 수정 요청 바디
 *
 * @property requesterId 친구 상태 수정을 요청한 사용자 ID
 * @property receiverId 친구 상태 수정 대상 사용자 ID
 */
@Serializable
data class UpdateFriendStatusRequest(
    val requesterId: Long,
    val receiverId: Long,
    val status: FriendStatus,
) {
    companion object {
        val sample = UpdateFriendStatusRequest(
            requesterId = 1,
            receiverId = 2,
            status = FriendStatus.ACCEPTED,
        )
    }
}
