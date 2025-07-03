package com.peekr.common.db.scheme

/** 소셜로그인 플랫폼 */
enum class SocialLoginProvider(val value: String) {
    Google("google"),
    Kakao("kakao"),
    Apple("apple"),
}

/**
 * 문자열을 알맞은 소셜로그인 플랫폼으로 변환
 *
 * @throws IllegalArgumentException 소셜로그인 플랫폼에 해당하지 않은 문자열 변환 시 예외 발생
 */
fun String.toSocialLoginProvider() = when (this) {
    "google" -> SocialLoginProvider.Google
    "kakao" -> SocialLoginProvider.Kakao
    "apple" -> SocialLoginProvider.Apple
    else -> throw IllegalArgumentException("Invalid social login provider: $this")
}
