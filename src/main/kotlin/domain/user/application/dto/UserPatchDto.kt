package com.peekr.domain.user.application.dto

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.domain.user.domain.model.UserPatch

/**
 * 애플리케이션 계층에서 사용하는 UserPatch DTO
 *
 * @param userName 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatchDto(
    val userName: UserName,
    val profileImageUrl: String?,
    val introduce: Introduce?,
)

fun UserPatchDto.toDomain(): UserPatch = UserPatch(
    userName = userName,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
