package com.turnin.domain.keyword.application.provider

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.KeywordDto
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.application.usecase.CreateKeywordUseCase
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import com.turnin.domain.keyword.exception.KeywordException

/**
 * 외부로 제공할 키워드 API
 */
class KeywordProviderApi(
    private val keywordRepository: KeywordRepository,
    private val createKeywordUseCase: CreateKeywordUseCase,
) {
    /**
     * 키워드 ID를 통해 키워드 DTO를 조회한다.
     *
     * @param keywordId 키워드 ID
     *
     * @return [KeywordDto] 키워드 DTO, 조회 실패 시 `null` 반환
     */
    suspend fun findById(keywordId: KeywordId): KeywordDto? =
        keywordRepository.findById(keywordId)?.toDto()

    /**
     * 키워드 ID 리스트를 통해 키워드를 조회한다.
     *
     * @param ids 키워드 ID 리스트
     */
    suspend fun findByIds(ids: List<KeywordId>): List<KeywordDto> =
        keywordRepository.findByIds(ids).map { it.toDto() }

    /**
     * 키워드 명을 통해 키워드 DTO를 조회한다.
     *
     * @param keywordName 키워드 명
     *
     * @return [KeywordDto] 키워드 DTO, 조회 실패 시 `null` 반환
     */
    suspend fun findByName(keywordName: String): KeywordDto? {
        val keywordNameVO = KeywordName(keywordName)
        return keywordRepository.findByName(keywordNameVO)?.toDto()
    }

    /**
     * 키워드를 생성한다.
     *
     * @param keywordName 키워드 명
     * @param createdBy 키워드를 생성한 사용자 ID
     *
     * @return [KeywordDto] 키워드 DTO
     *
     * @throws KeywordException.EmbeddingFailed 임베딩 과정에서 에러 발생 시 예외가 발생한다.
     */
    suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): KeywordDto =
        createKeywordUseCase(keywordName, createdBy)
}
