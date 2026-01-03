package com.peekr.domain.discover.infrastructure.provider

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.domain.discover.domain.provider.ExternalKeyword
import com.peekr.domain.discover.domain.provider.KeywordProvider
import com.peekr.domain.keyword.application.provider.KeywordProviderApi

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
