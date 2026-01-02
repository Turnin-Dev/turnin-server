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

    data object CountLimitReached : UserKeywordErrorCode(UK002, "키워드 개수 제한을 초과했습니다.")
}

private const val UK001 = "UK001"
private const val UK002 = "UK002"
