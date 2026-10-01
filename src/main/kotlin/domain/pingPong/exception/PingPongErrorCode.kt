package com.turnin.domain.pingPong.exception

import com.turnin.common.exception.ApiErrorCode

/**
 * 핑퐁 커스텀 에러 코드
 */
sealed class PingPongErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 질문을 등록할 게시물(사용자 키워드)을 찾을 수 없는 경우 */
    data object UserKeywordNotFound : PingPongErrorCode(PP001, "게시물을 찾을 수 없습니다.")

    /** 본인 게시물에 질문을 등록하려는 경우 */
    data object CannotQuestionOwnUserKeyword : PingPongErrorCode(PP002, "본인 게시물에는 질문을 등록할 수 없습니다.")
}

private const val PP001 = "PP001"
private const val PP002 = "PP002"
