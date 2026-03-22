package com.peekr.domain.friend.domain.message

/**
 * 친구 알림 메시지
 */
object FriendNotificationMessage {
    object FriendRequest {
        const val TITLE = "친구 요청"

        fun message(requesterName: String) = "${requesterName}님이 친구 요청을 보냈어요."
    }

    object FriendAccept {
        const val TITLE = "친구 수락"

        fun message(receiverName: String) = "${receiverName}님이 친구 요청을 수락했어요."
    }
}
