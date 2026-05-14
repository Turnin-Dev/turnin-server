package com.turnin.domain.userKeyword.infrastructure.provider

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.provider.KeywordProviderApi
import com.turnin.domain.userKeyword.domain.model.ExternalKeyword
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider

class KeywordProviderImpl(private val keywordProviderApi: KeywordProviderApi) : KeywordProvider {
    override suspend fun findById(keywordId: KeywordId): ExternalKeyword? {
        val keywordDto = keywordProviderApi.findById(keywordId)
        return keywordDto?.let {
            ExternalKeyword(
                id = it.id,
                name = KeywordName(it.name),
                createdBy = it.createdBy,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    override suspend fun findByIds(keywordIds: List<KeywordId>): List<ExternalKeyword> {
        val keywordDtoList = keywordProviderApi.findByIds(keywordIds)
        return keywordDtoList.map {
            ExternalKeyword(
                id = it.id,
                name = KeywordName(it.name),
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
                name = KeywordName(it.name),
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
            name = KeywordName(savedKeyword.name),
            createdBy = savedKeyword.createdBy,
            createdAt = savedKeyword.createdAt,
            updatedAt = savedKeyword.updatedAt,
        )
    }
}
