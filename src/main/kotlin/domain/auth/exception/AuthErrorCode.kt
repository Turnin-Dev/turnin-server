package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode

sealed class AuthErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 로그인 실패 에러 */
    data object LoginFailed :
        AuthErrorCode(A001, "로그인에 문제가 발생했습니다. (토큰 생성 실패)")

    /** 중복된 사용자 에러 */
    data object UserDuplicated :
        AuthErrorCode(A002, "중복된 사용자 입니다.")

    /** 리프레쉬 토큰 만료 에러 */
    data object RefreshTokenExpired :
        AuthErrorCode(A003, "토큰이 만료되었습니다.")

    /** 사용자 조회 실패 에러 */
    data object UserNotFound :
        AuthErrorCode(A004, "사용자를 찾을 수 없습니다.")

    /** 잘못된 형식의 PathParameter 에러 */
    data class PathParameterInvalid(val parameter: String) :
        AuthErrorCode(A005, "${parameter}의 입력 값 형식이 잘못되었습니다.")

    data object RefreshTokenSaveFailed :
        AuthErrorCode(A006, "토큰을 저장하는 도중 문제가 발생했습니다.")
}

private const val A001 = "A001"
private const val A002 = "A002"
private const val A003 = "A003"
private const val A004 = "A004"
private const val A005 = "A005"
private const val A006 = "A006"
