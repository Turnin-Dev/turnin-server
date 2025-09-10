package com.peekr.domain.keyword.domain.model

import com.peekr.domain.core.model.UserId

/**
 * 키워드
 *
 * @property id 키워드 ID
 * @property keyword 키워드명
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class Keyword(
    val id: KeywordId,
    val keyword: String,
    val createdBy: UserId,
    val createdAt: Long,
    val updatedAt: Long,
) {
    init {
        validateKeyword()
    }

    private fun validateKeyword() {
        require(this.keyword.length in KEYWORD_MIN_LENGTH..KEYWORD_MAX_LENGTH) {
            "키워드 길이 제한은 1~15자 이내 입니다."
        }
    }
}

private const val KEYWORD_MIN_LENGTH = 1
private const val KEYWORD_MAX_LENGTH = 15
