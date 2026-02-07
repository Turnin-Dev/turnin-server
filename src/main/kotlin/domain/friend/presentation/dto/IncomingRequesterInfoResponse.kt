package com.peekr.domain.friend.presentation.dto

import com.peekr.domain.friend.application.dto.IncomingRequesterInfoDto
import kotlinx.serialization.Serializable

/**
 * 받은 친구 요청자 정보 응답 바디
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
data class IncomingRequesterInfoResponse(
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
        val sample = IncomingRequesterInfoResponse(
            id = 1,
            userId = 2L,
            displayId = "did2",
            name = "name2",
            profileImageUrl = "http://www.example.com",
            respondedAt = 1697875200L,
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun IncomingRequesterInfoDto.toResponse(): IncomingRequesterInfoResponse =
    IncomingRequesterInfoResponse(
        id = id,
        userId = userId,
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
        respondedAt = respondedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
