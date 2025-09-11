package com.peekr.domain.userKeyword.domain.service

import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch

interface UserKeywordService {
    /**
     * 사용자 ID를 통해 사용자별 키워드 리스트를 조회한다.
     *
     * @param userId 사용자 ID
     *
     * @return 사용자별 키워드를 찾으면 [UserKeyword]리스트를 반환하고 만약 없다면 `빈 리스트`를 반환한다.
     */
    suspend fun getKeywords(userId: UserId): List<UserKeyword>

    /**
     * 사용자별 키워드를 생성한다.
     *
     * [keyword]가 기존에 존재하는지 확인하고 존재한다면 해당 키워드의 ID를 사용하고
     * 만약 없다면 새롭게 키워드를 등록한 후 등록한 키워드의 ID를 사용한다.
     *
     * @param keyword 키워드명
     * @param userId 사용자 ID
     * @param offsetX UI 좌표 상에서의 X 위치
     * @param offsetY UI 좌표 상에서의 Y 위치
     * @param description 키워드 개인 설명
     *
     * @return [UserKeyword] 사용자별 키워드를 반환한다.
     */
    suspend fun create(
        keyword: String,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword

    /**
     * 사용자별 키워드를 업데이트한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID
     * @param patch [UserKeywordPatch]
     *
     * @return [Boolean] 업데이트 성공 시 `true`, 실패 시 `false`를 반환한다.
     */
    suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
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
