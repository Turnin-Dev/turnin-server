package com.peekr.domain.block.presentation.dto

import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.block.application.dto.BlockedUserDto
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
data class BlockedUserResponse(
    val id: Long,
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
) {
    companion object {
        val sample = CursorPage(
            items = List(2) {
                BlockedUserResponse(
                    id = it + 1L,
                    userId = it + 1L,
                    displayId = "did${it + 1L}",
                    name = "name${it + 1L}",
                    profileImageUrl = "https://image-server.com/photo${it + 1L}",
                )
            },
            nextCursor = 2L,
        )
    }
}

fun BlockedUserDto.toResponse() =
    BlockedUserResponse(
        id = id,
        userId = userId,
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
    )
