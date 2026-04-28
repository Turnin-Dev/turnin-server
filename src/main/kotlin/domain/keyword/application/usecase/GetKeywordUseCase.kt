package com.turnin.domain.keyword.application.usecase

import com.turnin.common.model.id.KeywordId
import com.turnin.domain.keyword.application.dto.KeywordDto
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.repository.KeywordRepository

class GetKeywordUseCase(private val keywordRepository: KeywordRepository) {
    /**
     * 키워드 ID를 통해 키워드를 조회한다.
     *
     * @param id 키워드 ID
     *
     * @return 키워드가 이미 존재하면 저장된 [KeywordDto]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend operator fun invoke(id: KeywordId): KeywordDto? =
        keywordRepository.findById(id)?.toDto()
}
