package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.service.KeywordService

class GetKeywordByNameUseCase(private val keywordService: KeywordService) {
    suspend operator fun invoke(keywordName: String): KeywordDto? =
        keywordService.getKeywordByName(keywordName)?.toDto()
}
