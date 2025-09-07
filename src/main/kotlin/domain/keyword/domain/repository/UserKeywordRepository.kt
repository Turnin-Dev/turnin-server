package com.peekr.domain.keyword.domain.repository

import com.peekr.domain.common.model.UserId
import com.peekr.domain.keyword.domain.model.KeywordId
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId

interface UserKeywordRepository {
    /**
     * 키워드 ID와 사용자 ID를 통해 사용자별 키워드를 찾는다.
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     *
     * @return 사용자별 키워드를 찾으면 [UserKeyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findByKeywordIdAndUserId(keywordId: KeywordId, userId: UserId): UserKeyword?

    /**
     * 사용자별 키워드를 저장한다.
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     * @param offsetX UI 좌표 상에서의 X 위치
     * @param offsetY UI 좌표 상에서의 Y 위치
     * @param description 키워드 개인 설명
     *
     * @return [UserKeywordId] 사용자별 키워드 ID를 반환한다.
     */
    suspend fun save(
        keywordId: KeywordId,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeywordId

    /**
     * 사용자별 키워드를 업데이트한다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     * @param updates [Map]타입으로 key는 업데이트할 필드명이고, value는 업데이트할 값이다.
     *
     * @return [Boolean] 업데이트 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun update(
        userKeywordId: UserKeywordId,
        updates: Map<String, String>,
    ): Boolean

    /**
     * 사용자별 키워드를 삭제한다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 삭제 성공 시 `ture`, 실패 시 `false`를 반환한다.
     */
    suspend fun delete(userKeywordId: UserKeywordId): Boolean
}
