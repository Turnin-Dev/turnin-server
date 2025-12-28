package com.peekr.domain.keywordGraph.domain.provider

import com.peekr.common.model.id.KeywordId

/**
 * 외부에서 제공되는 키워드 BC API 인터페이스
 */
interface KeywordProvider {
    /**
     * 키워드 ID 리스트로 키워드 명 조회
     *
     * @param ids 키워드 ID 리스트
     *
     * @return [ExternalKeyword] 리스트
     */
    suspend fun findNameByIds(ids: List<KeywordId>): List<ExternalKeyword>
}
