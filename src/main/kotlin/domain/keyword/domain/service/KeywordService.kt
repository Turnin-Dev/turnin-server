package com.peekr.domain.keyword.domain.service

import com.peekr.domain.common.model.UserId
import com.peekr.domain.keyword.domain.model.KeywordId
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.UserKeywordPatch

interface KeywordService {
    /**
     * 사용자별 키워드 ID를 통해 사용자별 키워드를 찾는다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 사용자별 키워드를 찾으면 [UserKeyword]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findUserKeywordById(userKeywordId: UserKeywordId): UserKeyword?

    /**
     * 사용자별 키워드를 추가한다.
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     * @param offsetX UI 좌표 상에서의 X 위치
     * @param offsetY UI 좌표 상에서의 Y 위치
     * @param description 키워드 개인 설명
     *
     * @return [UserKeyword] 사용자별 키워드를 반환한다.
     */
    suspend fun addUserKeyword(
        keywordId: KeywordId,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword

    /**
     * 사용자별 키워드를 업데이트한다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     * @param patch [UserKeywordPatch]
     *
     * @return [Boolean] 업데이트 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun updateUserKeyword(
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean

    /**
     * 사용자별 키워드를 삭제한다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 삭제 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun delete(userKeywordId: UserKeywordId): Boolean
}
