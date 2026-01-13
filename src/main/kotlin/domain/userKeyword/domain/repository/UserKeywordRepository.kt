package com.peekr.domain.userKeyword.domain.repository

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordDetail

interface UserKeywordRepository {
    /**
     * 사용자 키워드 ID로 사용자 키워드 조회
     *
     * @param userKeywordId 사용자 키워드 ID
     */
    suspend fun findById(userKeywordId: UserKeywordId): UserKeyword?

    /**
     * 사용자 ID를 통해 사용자별 키워드 리스트를 조회한다.
     *
     * @param userId 사용자 ID
     */
    suspend fun findListByUserId(userId: UserId): List<UserKeyword>

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
     * 사용자 키워드 상세 정보를 조회한다.
     *
     * @param userKeywordId 사용자 키워드 ID
     * @param withUserInfo 사용자 정보 포함 여부
     *
     * @return [UserKeywordDetail]
     */
    suspend fun findUserKeywordDetail(
        userKeywordId: UserKeywordId,
        withUserInfo: Boolean,
    ): UserKeywordDetail?

    /**
     * 사용자 키워드 ID를 통해 사용자 키워드 설명을 조회한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return 사용자별 키워드를 찾으면 [Description]를 반환하고 만약 없다면 `null`을 반환한다.
     */
    suspend fun findDescriptionById(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Description?

    /**
     * 사용자 키워드 개수 카운트
     *
     * @param userId 사용자 ID
     */
    suspend fun countByUserId(userId: UserId): Long

    /**
     * 사용자별 키워드를 생성한다.
     *
     * @param keywordId 키워드 ID
     * @param userId 사용자 ID
     * @param description 키워드 개인 설명
     *
     * @return 생성된 [UserKeyword]를 반환한다.
     */
    suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        description: Description,
    ): UserKeyword

    /**
     * 사용자별 키워드 설명을 업데이트한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     * @param patch [Description]
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun updateDescription(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: Description,
    ): Boolean

    /**
     * 사용자별 키워드를 삭제한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 삭제 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordId): Boolean
}
