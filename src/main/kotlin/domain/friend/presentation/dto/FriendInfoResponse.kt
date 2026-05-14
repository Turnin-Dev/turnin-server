package com.turnin.domain.friend.presentation.dto

import com.turnin.domain.friend.application.dto.FriendInfoDto
import kotlinx.serialization.Serializable

/**
 * 친구 정보 응답 바디
 *
 * @property id 친구 ID
 * @property userId 사용자(친구) ID
 * @property displayId 사용자(친구) 표시 ID
 * @property name 사용자(친구) 이름
 * @property profileImageUrl 사용자(친구) 프로필 사진 url
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
@Serializable
data class FriendInfoResponse(
    val id: Long,
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = FriendInfoResponse(
            id = 1L,
            userId = 1L,
            displayId = "hong123",
            name = "honggd",
            profileImageUrl = "https://image-server.com/123123.jpg",
            respondedAt = 1697875200L,
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun FriendInfoDto.toResponse(): FriendInfoResponse =
    FriendInfoResponse(
        id = id,
        userId = userId,
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
        respondedAt = respondedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
