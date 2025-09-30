package com.peekr.domain.keyword.domain.model

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.validator.PeekrValidator.validation

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
        validation(isNotNullAndNotBlank(keyword)) { "키워드가 비어있습니다." }
        validation(isValidKeywordLength(keyword)) { "키워드 길이 제한은 1~15자 이내입니다." }
    }

    companion object {
        /** 키워드 최대 길이 */
        const val MAX_LENGTH = 15

        /** 키워드 최소 길이 */
        const val MIN_LENGTH = 1

        /** 키워드 null 혹은 공백 체크 */
        fun isNotNullAndNotBlank(keyword: String?): Boolean =
            keyword != null && keyword.isNotBlank()

        fun isValidKeywordLength(keyword: String): Boolean =
            keyword.length in MIN_LENGTH..MAX_LENGTH
    }
}
