package com.peekr.domain.keyword.infrastructure.service.impl

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.domain.service.KeywordService

class KeywordServiceImpl(private val keywordRepository: KeywordRepository) : KeywordService {
    override suspend fun getKeyword(id: KeywordId): Keyword? = keywordRepository.findById(id)

    override suspend fun create(
        keyword: String,
        createdBy: UserId,
    ): Keyword = keywordRepository.create(keyword, createdBy)
}
