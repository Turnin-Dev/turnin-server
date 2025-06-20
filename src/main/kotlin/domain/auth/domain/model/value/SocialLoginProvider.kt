package com.peekr.domain.auth.domain.model.value

/** 소셜로그인 제공 플랫폼 */
enum class SocialLoginProvider {
    Google,
    Kakao,
    Apple,
}

fun String.toSocialLoginProvider() = when (this) {
    "Google" -> SocialLoginProvider.Google
    "Kakao" -> SocialLoginProvider.Kakao
    "Apple" -> SocialLoginProvider.Apple
    else -> throw IllegalArgumentException("Invalid social login provider: $this")
}
