package com.turnin.domain.keyword.domain.repository

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.domain.model.Keyword

interface KeywordRepository {
    /**
     * 키워드 ID를 통해 키워드가 존재하는지 찾는다.
     *
     * @param id 키워드 ID
     *
     * @return 키워드가 이미 존재하면 저장된 [Keyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findById(id: KeywordId): Keyword?

    /**
     * 키워드 ID 리스트를 통해 키워드를 조회한다.
     *
     * @param ids 키워드 ID 리스트
     */
    suspend fun findByIds(ids: List<KeywordId>): List<Keyword>

    /**
     * 키워드 명을 통해 키워드가 존재하는지 찾는다.
     *
     * @param keywordName 키워드 명
     *
     * @return 키워드가 이미 존재하면 저장된 [Keyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findByName(keywordName: KeywordName): Keyword?

    /**
     * 키워드를 생성하고 성공 시 키워드를 반환한다.
     *
     * @param keywordName 키워드명
     * @param embeddedKeyword 임베드된 키워드
     * @param createdBy 키워드 최초 등록자
     *
     * @return 생성된 [Keyword] 키워드를 반환한다.
     */
    suspend fun create(
        keywordName: KeywordName,
        embeddedKeyword: String,
        createdBy: UserId,
    ): Keyword
}
