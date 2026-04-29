package com.turnin.domain.keyword.infrastructure.provider

import com.turnin.common.ml.EmbeddingService
import com.turnin.common.ml.EmbeddingServiceException
import com.turnin.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.turnin.domain.keyword.exception.KeywordException

class EmbeddingServiceProviderImpl(private val embeddingService: EmbeddingService) : EmbeddingServiceProvider {
    override fun embed(text: String): String =
        try {
            embeddingService.embed(text)
        } catch (e: EmbeddingServiceException) {
            throw KeywordException.EmbeddingFailed(e)
        }
}
