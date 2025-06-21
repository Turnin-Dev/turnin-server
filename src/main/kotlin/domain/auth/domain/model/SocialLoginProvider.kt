package com.peekr.domain.auth.domain.model

/** 소셜로그인 플랫폼 */
enum class SocialLoginProvider {
    Google,
    Kakao,
    Apple,
}

/**
 * 문자열을 알맞은 소셜로그인 플랫폼으로 변환
 *
 * @throws IllegalArgumentException 소셜로그인 플랫폼에 해당하지 않은 문자열 변환 시 예외 발생
 */
fun String.toSocialLoginProvider() = when (this) {
    "Google" -> SocialLoginProvider.Google
    "Kakao" -> SocialLoginProvider.Kakao
    "Apple" -> SocialLoginProvider.Apple
    else -> throw IllegalArgumentException("Invalid social login provider: $this")
}
