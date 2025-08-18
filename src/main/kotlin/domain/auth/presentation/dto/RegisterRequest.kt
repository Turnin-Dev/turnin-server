package com.peekr.domain.auth.presentation.dto

import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.domain.auth.presentation.validation.validateDisplayId
import com.peekr.domain.auth.presentation.validation.validateIntroduce
import com.peekr.domain.auth.presentation.validation.validateName
import com.peekr.domain.auth.presentation.validation.validateProfileImageUrl
import com.peekr.domain.auth.presentation.validation.validateProviderId
import kotlinx.serialization.Serializable

/**
 * 회원가입 요청 바디
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 * @property introduce 사용자 소개 글
 */
@Serializable
data class RegisterRequest(
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
) {
    companion object {
        val sample = RegisterRequest(
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
            introduce = "Hello!",
        )
    }
}

fun RegisterRequest.validate() {
    providerId.validateProviderId()
    name.validateName()
    displayId.validateDisplayId()
    profileImageUrl?.validateProfileImageUrl()
    introduce?.validateIntroduce()
}
