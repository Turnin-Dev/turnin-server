package com.peekr.domain.keyword.domain.service

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword

interface KeywordService {
    /**
     * 키워드 ID를 통해 키워드를 조회한다.
     *
     * @param id 키워드 ID
     *
     * @return 키워드가 이미 존재하면 저장된 [Keyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun getKeyword(id: KeywordId): Keyword?

    /**
     * 키워드 명을 통해 키워드가 존재하는지 찾는다.
     *
     * @param keywordName 키워드 명
     *
     * @return 키워드가 이미 존재하면 저장된 [Keyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun getKeywordByName(keywordName: String): Keyword?

    /**
     * 키워드를 생성한다.
     *
     * @param keyword 키워드명
     * @param createdBy 키워드 최초 등록자
     *
     * @return 생성된 [Keyword] 키워드를 반환한다.
     */
    suspend fun create(keyword: String, createdBy: UserId): Keyword
}
