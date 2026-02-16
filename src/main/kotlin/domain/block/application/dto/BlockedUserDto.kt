package com.peekr.domain.block.application.dto

import com.peekr.domain.block.domain.model.BlockedUser

/**
 * 차단 사용자 DTO
 *
 * @property id 차단 ID
 * @property userId 차단한 사용자 ID
 * @property displayId 차단한 사용자 표시 ID
 * @property name 차단한 사용자 명
 * @property profileImageUrl 차단한 사용자 프로필 사진 url
 */
data class BlockedUserDto(
    val id: Long,
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
)

fun BlockedUser.toDto(): BlockedUserDto =
    BlockedUserDto(
        id = id.value,
        userId = userId.value,
        displayId = displayId.value,
        name = name.value,
        profileImageUrl = profileImageUrl,
    )
