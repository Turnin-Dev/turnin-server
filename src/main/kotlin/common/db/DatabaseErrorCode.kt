package com.peekr.common.db

import com.peekr.common.exception.ApiErrorCode

/**
 * 데이터베이스 에러코드
 *
 * @property code 에러코드
 * @property description 에러코드 설명
 *
 * @see ApiErrorCode
 */
sealed class DatabaseErrorCode(
    code: String,
    description: String,
) : ApiErrorCode(code, description) {
    data object DBQueryError :
        DatabaseErrorCode(DB001, "DB 쿼리에서 오류가 발생했습니다.")
}

private const val DB001 = "DB001"
