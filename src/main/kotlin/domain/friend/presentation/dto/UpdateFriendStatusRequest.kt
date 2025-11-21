package com.peekr.domain.friend.presentation.dto

import com.peekr.common.model.FriendStatus
import kotlinx.serialization.Serializable

/**
 * 친구 상태 수정 요청 바디
 *
 * @property receiverId 친구 상태 수정 대상 사용자 ID
 * @property status 수정할 친구 상태
 */
@Serializable
data class UpdateFriendStatusRequest(
    val receiverId: Long,
    val status: FriendStatus,
) {
    companion object {
        val sample = UpdateFriendStatusRequest(
            receiverId = 2,
            status = FriendStatus.ACCEPTED,
        )
    }
}
