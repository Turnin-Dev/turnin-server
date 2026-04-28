package com.turnin.domain.discover.presentation.dto

import com.turnin.domain.discover.application.dto.DiscoverUserDto
import kotlinx.serialization.Serializable

/**
 * 탐색용 사용자 응답 바디
 *
 * @property id 사용자 ID
 * @property name 사용자 명
 * @property displayId 사용자 표시 ID
 * @property profileImageUrl 사용자 프로필 url
 */
@Serializable
data class DiscoverUserResponse(
    val id: Long,
    val name: String,
    val displayId: String,
    val profileImageUrl: String?,
)

fun DiscoverUserDto.toResponse() =
    DiscoverUserResponse(
        id = id.value,
        name = name,
        displayId = displayId,
        profileImageUrl = profileImageUrl,
    )
