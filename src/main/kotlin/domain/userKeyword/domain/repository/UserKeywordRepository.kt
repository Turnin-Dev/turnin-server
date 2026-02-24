package com.peekr.domain.userKeyword.domain.repository

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordDetail
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch

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
     * 사용자 키워드 ID로 사용자 키워드 상세 정보를 조회한다. (차단된 사용자, 비활성화 사용자 제외)
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userKeywordId 사용자 키워드 ID
     *
     * @return [UserKeywordDetail]
     */
    suspend fun getDetailById(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
    ): UserKeywordDetail?

    /**
     * 사용자 ID로 사용자의 키워드 상세 정보 리스트를 조회한다. (차단된 사용자, 비활성화 사용자 제외)
     *
     * @param currentUserId 현재 조회 요청한 사용자 ID
     * @param userId 사용자 ID
     */
    suspend fun getDetailsByUserId(
        currentUserId: UserId,
        userId: UserId,
    ): List<UserKeywordDetail>

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
     * 사용자 키워드를 업데이트한다.
     *
     * @param ownerId 사용자 ID
     * @param patch [UserKeywordPatch]
     *
     * @return 성공 시 `true`, 실패 시 `false` 반환
     */
    suspend fun update(
        ownerId: UserId,
        patch: UserKeywordPatch,
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
