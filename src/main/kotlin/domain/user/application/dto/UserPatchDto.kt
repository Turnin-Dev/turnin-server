package com.peekr.domain.user.application.dto

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.domain.user.domain.model.UserPatch

/**
 * 애플리케이션 계층에서 사용하는 UserPatch DTO
 *
 * @param userName 사용자 이름
 * @param displayId 사용자 표시 ID
 * @param oldProfileImageUrl 기존 사용자 프로필 이미지 url
 * @param newProfileImageUrl 새로운 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatchDto(
    val userName: String,
    val displayId: String,
    val oldProfileImageUrl: String?,
    val newProfileImageUrl: String?,
    val introduce: String,
)

fun UserPatchDto.toDomain(): UserPatch = UserPatch(
    userName = UserName(userName),
    displayId = DisplayId(displayId),
    oldProfileImageUrl = oldProfileImageUrl,
    newProfileImageUrl = newProfileImageUrl,
    introduce = Introduce(introduce),
)
