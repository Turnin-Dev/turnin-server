package com.peekr.domain.friend.presentation.dto

import com.peekr.domain.friend.application.dto.FriendInfoDto
import kotlinx.serialization.Serializable

/**
 * 친구 정보 응답 바디
 *
 * @property id 친구 ID
 * @property userId 사용자(친구) ID
 * @property name 사용자(친구) 이름
 * @property profileImageUrl 사용자(친구) 프로필 사진 url
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
@Serializable
data class FriendInfosResponse(
    val id: Long,
    val userId: Long,
    val name: String,
    val profileImageUrl: String?,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = FriendInfosResponse(
            id = 1L,
            userId = 1L,
            name = "honggd",
            profileImageUrl = "https://image-server.com/123123.jpg",
            respondedAt = 1682870400000,
            createdAt = 1682870400000,
            updatedAt = 1682870400000,
        )
    }
}

fun FriendInfoDto.toResponse(): FriendInfosResponse =
    FriendInfosResponse(
        id = id,
        userId = userId,
        name = name,
        profileImageUrl = profileImageUrl,
        respondedAt = respondedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
