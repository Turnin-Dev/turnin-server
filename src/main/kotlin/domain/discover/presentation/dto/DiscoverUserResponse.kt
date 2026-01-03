package com.peekr.domain.discover.presentation.dto

import com.peekr.domain.discover.application.dto.DiscoverUserDto
import kotlinx.serialization.Serializable

/**
 * 탐색용 사용자 응답 바디
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
@Serializable
data class DiscoverUserResponse(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
)

fun DiscoverUserDto.toResponse() =
    DiscoverUserResponse(
        userId = userId.value,
        userName = userName,
        profileImageUrl = profileImageUrl,
    )
