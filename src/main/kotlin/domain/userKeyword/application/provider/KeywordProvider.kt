package com.peekr.domain.userKeyword.application.provider

import com.peekr.common.model.KeywordId
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.userKeyword.application.dto.ExternalKeyword

/**
 * 키워드 BC(Bounded Context)에서 제공되는 서비스
 *
 * 내부적으로는 키워드 BC(Bounded Context)에서 제공되는 공개용 API만 사용한다.
 */
class KeywordProvider(private val keywordRepository: KeywordRepository) {
    suspend fun getKeywordById(keywordId: KeywordId): ExternalKeyword? =
        keywordRepository.findById(keywordId)?.let { keyword ->
            ExternalKeyword(
                id = keyword.id,
                keyword = keyword.keyword,
                createdBy = keyword.createdBy,
                createdAt = keyword.createdAt,
                updatedAt = keyword.updatedAt,
            )
        }
}
