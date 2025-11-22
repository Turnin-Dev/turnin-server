package com.peekr.domain.user.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId

/**
 * UserPatch
 *
 * @param displayId 사용자 표시 ID
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatch(
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
)
