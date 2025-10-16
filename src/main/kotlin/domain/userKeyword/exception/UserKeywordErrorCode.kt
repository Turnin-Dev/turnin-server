package com.peekr.domain.userKeyword.exception

import com.peekr.common.exception.ApiErrorCode

/**
 * 사용자 키워드 커스텀 에러 코드
 */
sealed class UserKeywordErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 존재하지 않은 키워드 조회 에러 */
    data object NotExistsKeyword : UserKeywordErrorCode(UK001, "존재하지 않은 키워드입니다.")
}

private const val UK001 = "UK001"
