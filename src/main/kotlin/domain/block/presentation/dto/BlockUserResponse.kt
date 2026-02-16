package com.peekr.domain.block.presentation.dto

import com.peekr.domain.block.application.dto.BlockUserDto
import kotlinx.serialization.Serializable

/**
 * 차단 사용자 응답 바디
 *
 * @property id 차단 ID
 * @property userId 차단한 사용자 ID
 * @property displayId 차단한 사용자 표시 ID
 * @property name 차단한 사용자 명
 * @property profileImageUrl 차단한 사용자 프로필 사진 url
 */
@Serializable
data class BlockUserResponse(
    val id: Long,
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
) {
    companion object {
        val sample = BlockUserResponse(
            id = 1,
            userId = 1,
            displayId = "DisplayID",
            name = "Username",
            profileImageUrl = "https://image-server.com/photo.jpg",
        )
    }
}

fun BlockUserDto.toResponse() =
    BlockUserResponse(
        id = id,
        userId = userId,
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
    )
