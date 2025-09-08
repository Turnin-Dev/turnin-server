package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.keyword.application.dto.UserKeywordDto
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.UserKeywordPatch

interface UserKeywordUseCase {
    /**
     * 사용자별 키워드를 추가한다.
     *
     * @param userKeywordDto [UserKeywordDto] 사용자별 키워드 DTO
     *
     * @return [UserKeyword] 사용자별 키워드
     */
    suspend fun add(userKeywordDto: UserKeywordDto): UserKeyword

    /**
     * 사용자 ID로 [UserKeyword]리스트를 조회한다.
     *
     * @param userId 사용자 ID
     *
     * @return [UserKeyword] 리스트를 반환한다.
     */
    suspend fun findById(userId: Long): List<UserKeyword>

    /**
     * 사용자별 키워드를 업데이트한다.
     *
     * @param userKeywordId [UserKeywordId] 사용자별 키워드 ID
     * @param patch [UserKeywordPatch]
     *
     * @return 업데이트 성공 시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend fun update(
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean

    /**
     * 사용자별 키워드 ID로 사용자별 키워드를 삭제한다.
     *
     * @param userKeywordId 사용자별 키워드 ID
     *
     * @return 삭제 성공시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend fun delete(userKeywordId: UserKeywordId): Boolean
}
