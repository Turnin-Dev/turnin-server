package com.peekr.domain.keyword.domain.repository

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword

interface KeywordRepository {
    /**
     * 키워드명을 통해 키워드가 존재하는지 찾는다.
     *
     * @param keyword 키워드명
     *
     * @return 키워드가 이미 존재하면 저장된 [Keyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findByKeyword(keyword: String): Keyword?

    /**
     * 키워드를 생성하고 성공 시 키워드를 반환한다.
     *
     * @param keyword 키워드명
     * @param createdBy 키워드 최초 등록자
     *
     * @return 생성된 [Keyword] 키워드를 반환한다.
     */
    suspend fun create(keyword: String, createdBy: UserId): Keyword
}
