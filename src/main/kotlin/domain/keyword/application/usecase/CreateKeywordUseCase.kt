package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.service.KeywordService

/**
 * 키워드를 생성한다.
 */
class CreateKeywordUseCase(private val keywordService: KeywordService) {
    /**
     * @param keyword 키워드명
     * @param createdBy 키워드 최초등록자 ID
     *
     * @return [KeywordDto]
     */
    suspend operator fun invoke(
        keyword: String,
        createdBy: UserId,
    ): KeywordDto = keywordService.create(keyword, createdBy).toDto()
}
