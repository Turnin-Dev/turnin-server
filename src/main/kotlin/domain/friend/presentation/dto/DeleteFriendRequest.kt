package com.peekr.domain.friend.presentation.dto

/**
 * 친구 삭제 요청 바디
 *
 * @property requesterId 친구 상태 수정을 요청한 사용자 ID
 * @property receiverId 친구 상태 수정 대상 사용자 ID
 */
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
