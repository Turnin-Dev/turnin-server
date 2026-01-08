package com.peekr.domain.keyword.infrastructure.provider

import com.peekr.common.ml.EmbeddingService
import com.peekr.common.ml.EmbeddingServiceException
import com.peekr.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.peekr.domain.keyword.exception.KeywordException

class EmbeddingServiceProviderImpl(private val embeddingService: EmbeddingService) : EmbeddingServiceProvider {
    override fun embed(text: String): String =
        try {
            embeddingService.embed(text)
        } catch (e: EmbeddingServiceException) {
            throw KeywordException.EmbeddingFailed(e)
        }
}
