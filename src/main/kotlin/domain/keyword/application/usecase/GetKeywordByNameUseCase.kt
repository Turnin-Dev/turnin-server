package com.peekr.domain.keyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.repository.KeywordRepository

class GetKeywordByNameUseCase(private val keywordRepository: KeywordRepository) {
    /**
     * 키워드 명을 통해 키워드가 존재하는지 찾는다.
     *
     * @param keywordName 키워드 명
     *
     * @return 키워드가 이미 존재하면 저장된 [KeywordDto]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend operator fun invoke(keywordName: String): KeywordDto? {
        val keywordNameVO = KeywordName(keywordName)
        return keywordRepository.findByName(keywordNameVO)?.toDto()
    }
}
