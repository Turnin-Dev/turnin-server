package com.peekr.domain.keywordGraph.exception

import com.peekr.common.exception.ApiErrorCode

/**
 * 키워드 그래프 커스텀 에러 코드
 */
sealed class KeywordGraphErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object KeywordIdPairingFailed : KeywordGraphErrorCode(KG001, "키워드 페어링 오류입니다.")

    data object UserNotFound : KeywordGraphErrorCode(KG002, "사용자를 찾지 못했습니다.")
}

private const val KG001 = "KG001"
private const val KG002 = "KG002"
