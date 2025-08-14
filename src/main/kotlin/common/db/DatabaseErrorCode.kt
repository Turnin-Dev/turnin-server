package com.peekr.common.db

import com.peekr.common.exception.ApiErrorCode

/**
 * 데이터베이스 에러코드
 *
 * 모든 데이터베이스 에러코드는 `DBxxx` 포맷을 따릅니다.(예: DB001)
 *
 * @property code 에러코드
 * @property description 에러코드 설명 (클라이언트 메시지, 로그용 설명)
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
