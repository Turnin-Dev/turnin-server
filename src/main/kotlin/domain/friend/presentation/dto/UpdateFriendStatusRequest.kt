package com.turnin.domain.friend.presentation.dto

import com.turnin.common.model.FriendRequestStatus
import kotlinx.serialization.Serializable

/**
 * 친구 상태 수정 요청 바디
 *
 * @property requesterId 친구 상태 수정을 요청한 사용자 ID
 * @property receiverId 친구 상태 수정 대상 사용자 ID
 * @property requestStatus 수정할 친구 요청 상태
 */
@Serializable
data class UpdateFriendStatusRequest(
    val requesterId: Long,
    val receiverId: Long,
    val requestStatus: FriendRequestStatus,
) {
    companion object {
        val sample = UpdateFriendStatusRequest(
            requesterId = 1,
            receiverId = 2,
            requestStatus = FriendRequestStatus.ACCEPTED,
        )
    }
}
