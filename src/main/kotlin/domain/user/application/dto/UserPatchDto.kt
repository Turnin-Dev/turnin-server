package com.peekr.domain.user.application.dto

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.domain.user.domain.model.UserPatch

/**
 * 애플리케이션 계층에서 사용하는 UserPatch DTO
 *
 * @param displayId 사용자 표시 ID
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatchDto(
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: String?,
)

fun UserPatchDto.toDomain(): UserPatch = UserPatch(
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
