package com.peekr.domain.discover.application.dto

import com.peekr.common.model.id.UserId

/**
 * 탐색용 사용자 DTO
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
data class DiscoverUserDto(
    val userId: UserId,
    val userName: String,
    val profileImageUrl: String?,
)
