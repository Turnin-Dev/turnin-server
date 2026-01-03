package com.peekr.domain.user.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name

/**
 * UserPatch
 *
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 */
data class UserPatch(
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
)
