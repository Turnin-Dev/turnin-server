package com.peekr.domain.keyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.repository.KeywordRepository

class CreateKeywordUseCase(private val keywordRepository: KeywordRepository) {
    /**
     * 키워드를 생성한다.
     *
     * @param keywordName 키워드명
     * @param createdBy 키워드 최초등록자 ID
     *
     * @return [KeywordDto] 키워드 DTO를 반환한다
     */
    suspend operator fun invoke(
        keywordName: String,
        createdBy: UserId,
    ): KeywordDto {
        val keywordNameVO = KeywordName(keywordName)
        return keywordRepository.create(keywordNameVO, createdBy).toDto()
    }
}
