package com.peekr.domain.userKeyword.domain.provider

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId

/**
 * 키워드 BC(Bounded Context)에서 제공되는 서비스
 *
 * 내부적으로는 키워드 BC(Bounded Context)에서 제공되는 공개용 API만 사용한다.
 */
interface KeywordProvider {
    /**
     * 키워드 ID로 키워드 조회
     *
     * @param [keywordId] 키워드 ID
     * @return [ExternalKeyword]
     */
    suspend fun findById(keywordId: KeywordId): ExternalKeyword?

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
