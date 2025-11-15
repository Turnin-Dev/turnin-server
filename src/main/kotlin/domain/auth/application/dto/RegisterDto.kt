package com.peekr.domain.auth.application.dto

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth

/**
 * 애플리케이션 계층에서 사용하는 Register
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 * @property introduce 사용자 소개 글
 */
data class RegisterDto(
    val provider: SocialLoginProviderForAuth,
    val providerId: String,
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
)
