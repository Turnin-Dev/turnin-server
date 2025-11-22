package com.peekr.domain.userKeyword.infrastructure.provider

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keyword.application.provider.KeywordProviderApi
import com.peekr.domain.userKeyword.domain.provider.ExternalKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider

class KeywordProviderImpl(private val keywordProviderApi: KeywordProviderApi) : KeywordProvider {
    override suspend fun findById(keywordId: KeywordId): ExternalKeyword? {
        val keywordDto = keywordProviderApi.findById(keywordId)
        return keywordDto?.let {
            ExternalKeyword(
                id = it.id,
                name = it.name,
                createdBy = it.createdBy,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    override suspend fun findByName(keywordName: String): ExternalKeyword? {
        val keywordDto = keywordProviderApi.findByName(keywordName)
        return keywordDto?.let {
            ExternalKeyword(
                id = it.id,
                name = it.name,
                createdBy = it.createdBy,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    override suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): ExternalKeyword {
        val savedKeyword = keywordProviderApi.create(keywordName, createdBy)
        return ExternalKeyword(
            id = savedKeyword.id,
            name = savedKeyword.name,
            createdBy = savedKeyword.createdBy,
            createdAt = savedKeyword.createdAt,
            updatedAt = savedKeyword.updatedAt,
        )
    }
}
