package com.peekr.domain.user.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId

/**
 * UserPatch
 *
 * @param userName 사용자 이름
 * @param displayId 사용자 표시 ID
 * @param oldProfileImageUrl 기존 사용자 프로필 이미지 url
 * @param newProfileImageUrl 새로운 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatch(
    val userName: UserName,
    val displayId: DisplayId,
    val oldProfileImageUrl: String?,
    val newProfileImageUrl: String?,
    val introduce: Introduce,
)
