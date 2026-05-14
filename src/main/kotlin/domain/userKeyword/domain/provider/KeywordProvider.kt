package com.turnin.domain.userKeyword.domain.provider

import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.domain.model.ExternalKeyword

/**
 * 외부에서 제공되는 키워드 API
 */
interface KeywordProvider {
    /**
     * 키워드 ID로 키워드 조회
     *
     * @param [keywordId] 키워드 ID
     * @return [com.turnin.domain.userKeyword.domain.model.ExternalKeyword]
     */
    suspend fun findById(keywordId: KeywordId): ExternalKeyword?

    /**
     * 키워드 ID 리스트로 키워드 리스트 조회
     *
     * @param keywordIds 키워드 ID 리스트
     */
    suspend fun findByIds(keywordIds: List<KeywordId>): List<ExternalKeyword>

    /**
     * 키워드 명으로 키워드 조회
     *
     * @param keywordName 키워드 명
     * @return [ExternalKeyword]
     */
    suspend fun findByName(keywordName: String): ExternalKeyword?

    /**
     * 키워드 생성
     *
     * @param keywordName 키워드 명
     * @param createdBy 키워드 생성자 사용자 ID
     */
    suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): ExternalKeyword
}
