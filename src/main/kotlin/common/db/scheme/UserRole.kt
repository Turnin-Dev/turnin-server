package com.peekr.common.db.scheme

/** 사용자 역할 */
enum class UserRole {
    USER,
    ADMIN,
}

/**
 * 문자열을 알맞은 사용자 역할로 변환
 *
 * @throws IllegalArgumentException 사용자 역할에 해당하지 않은 문자열 변환 시 예외 발생
 */
fun String.toUserRole() = when (this) {
    UserRole.USER.name -> UserRole.USER
    UserRole.ADMIN.name -> UserRole.ADMIN
    else -> throw IllegalArgumentException("Invalid user role: $this")
}
