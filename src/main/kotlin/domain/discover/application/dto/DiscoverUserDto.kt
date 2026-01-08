package com.peekr.domain.discover.application.dto

import com.peekr.common.model.id.UserId

/**
 * 탐색용 사용자 DTO
 *
 * @property id 사용자 ID
 * @property name 사용자 명
 * @property displayId 사용자 표시 ID
 * @property profileImageUrl 사용자 프로필 url
 */
data class DiscoverUserDto(
    val id: UserId,
    val name: String,
    val displayId: String,
    val profileImageUrl: String?,
)
