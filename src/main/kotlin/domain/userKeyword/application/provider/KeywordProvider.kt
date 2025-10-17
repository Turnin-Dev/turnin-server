package com.peekr.domain.userKeyword.application.provider

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.userKeyword.application.dto.ExternalKeyword

/**
 * 키워드 BC(Bounded Context)에서 제공되는 서비스
 *
 * 내부적으로는 키워드 BC(Bounded Context)에서 제공되는 공개용 API만 사용한다.
 */
class KeywordProvider(private val keywordRepository: KeywordRepository) {
    /**
     * 키워드 ID로 키워드 조회
     *
     * @param [keywordId] 키워드 ID
     * @return [ExternalKeyword]
     */
    suspend fun findById(keywordId: KeywordId): ExternalKeyword? =
        keywordRepository.findById(keywordId)?.let { keyword ->
            ExternalKeyword(
                id = keyword.id,
                keyword = keyword.keyword,
                createdBy = keyword.createdBy,
                createdAt = keyword.createdAt,
                updatedAt = keyword.updatedAt,
            )
        }

    /**
     * 키워드 명으로 키워드 조회
     *
     * @param keywordName 키워드 명
     * @return [ExternalKeyword]
     */
    suspend fun findByName(keywordName: String): ExternalKeyword? =
        keywordRepository.findByName(keywordName)?.let { keyword ->
            ExternalKeyword(
                id = keyword.id,
                keyword = keyword.keyword,
                createdBy = keyword.createdBy,
                createdAt = keyword.createdAt,
                updatedAt = keyword.updatedAt,
            )
        }

    /**
     * 키워드 생성
     *
     * @param keywordName 키워드 명
     * @param createdBy 키워드 생성자 사용자 ID
     */
    suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): ExternalKeyword {
        val savedKeyword = keywordRepository.create(keywordName, createdBy)
        return ExternalKeyword(
            id = savedKeyword.id,
            keyword = savedKeyword.keyword,
            createdBy = savedKeyword.createdBy,
            createdAt = savedKeyword.createdAt,
            updatedAt = savedKeyword.updatedAt,
        )
    }
}
