package com.peekr.domain.keywordGraph.infrastructure.provider

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.domain.keyword.application.provider.KeywordProviderApi
import com.peekr.domain.keywordGraph.domain.provider.ExternalKeyword
import com.peekr.domain.keywordGraph.domain.provider.KeywordProvider

class KeywordProviderImpl(private val keywordProviderApi: KeywordProviderApi) : KeywordProvider {
    override suspend fun findByIds(ids: List<KeywordId>): List<ExternalKeyword> =
        keywordProviderApi.findByIds(ids).map {
            ExternalKeyword(
                id = it.id,
                name = KeywordName(it.name),
                createdBy = it.createdBy,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
}
