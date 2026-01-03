package com.peekr.domain.discover.domain.provider

import com.peekr.common.model.id.KeywordId

/**
 * 외부에서 제공되는 키워드 BC API 인터페이스
 */
interface KeywordProvider {
    /**
     * 키워드 ID 리스트로 키워드 조회
     *
     * @param ids 키워드 ID 리스트
     *
     * @return [ExternalKeyword] 리스트
     */
    suspend fun findByIds(ids: List<KeywordId>): List<ExternalKeyword>
}
