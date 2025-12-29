package com.peekr.domain.keyword.application.provider

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.repository.KeywordRepository

/**
 * 외부로 제공할 키워드 API
 */
class KeywordProviderApi(private val keywordRepository: KeywordRepository) {
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
     */
    suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): KeywordDto {
        val keywordNameVO = KeywordName(keywordName)
        val savedKeyword = keywordRepository.create(keywordNameVO, createdBy)
        return savedKeyword.toDto()
    }
}
