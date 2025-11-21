package com.peekr.domain.friend.presentation.dto

import com.peekr.domain.friend.application.dto.FriendDto
import kotlinx.serialization.Serializable

/**
 * 친구 목록 응답 바디
 *
 * @property friends 친구 목록
 */
@Serializable
data class FriendsResponse(val friends: List<FriendResponse>) {
    companion object {
        val sample = FriendsResponse(
            friends = listOf(FriendResponse.sample),
        )
    }
}

fun List<FriendDto>.toResponse(): FriendsResponse =
    FriendsResponse(this.map { it.toResponse() })
