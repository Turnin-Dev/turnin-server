package com.peekr.domain.friend.domain.model

import com.peekr.common.model.id.UserId

/**
 * 친구 추가 엔티티 모델
 */
data class AddFriend(
    val requesterId: UserId,
    val receiverId: UserId,
)
