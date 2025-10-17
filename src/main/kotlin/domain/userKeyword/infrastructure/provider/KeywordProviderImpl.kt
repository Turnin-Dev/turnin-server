package com.peekr.domain.userKeyword.infrastructure.provider

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.userKeyword.domain.provider.ExternalKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider

class KeywordProviderImpl(private val keywordRepository: KeywordRepository) : KeywordProvider {
    override suspend fun findById(keywordId: KeywordId): ExternalKeyword? =
        keywordRepository.findById(keywordId)?.let { keyword ->
            ExternalKeyword(
                id = keyword.id,
                keyword = keyword.keyword,
                createdBy = keyword.createdBy,
                createdAt = keyword.createdAt,
                updatedAt = keyword.updatedAt,
            )
        }

    override suspend fun findByName(keywordName: String): ExternalKeyword? =
        keywordRepository.findByName(keywordName)?.let { keyword ->
            ExternalKeyword(
                id = keyword.id,
                keyword = keyword.keyword,
                createdBy = keyword.createdBy,
                createdAt = keyword.createdAt,
                updatedAt = keyword.updatedAt,
            )
        }

    override suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): ExternalKeyword {
        val savedKeyword = keywordRepository.create(keywordName, createdBy)
        return ExternalKeyword(
            id = savedKeyword.id,
            keyword = savedKeyword.keyword,
            createdBy = savedKeyword.createdBy,
            createdAt = savedKeyword.createdAt,
            updatedAt = savedKeyword.updatedAt,
        )
    }
}
