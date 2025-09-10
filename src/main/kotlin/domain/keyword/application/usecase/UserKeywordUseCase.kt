package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordIdDto
import com.peekr.domain.keyword.application.dto.UserKeywordPatchDto

interface UserKeywordUseCase {
    /**
     * 사용자별 키워드를 추가한다.
     *
     * @param createUserKeywordDto [CreateUserKeywordDto] 사용자별 키워드 DTO
     *
     * @return [UserKeywordDto] 사용자별 키워드 DTO
     */
    suspend fun create(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto

    /**
     * 사용자 ID로 사용자별 키워드 리스트를 조회한다.
     *
     * @param userId 사용자 ID
     *
     * @return [UserKeywordDto] 리스트를 반환한다.
     */
    suspend fun getListById(userId: UserId): List<UserKeywordDto>

    /**
     * 사용자별 키워드를 업데이트한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId [UserKeywordIdDto] 사용자별 키워드 ID DTO
     * @param patch [UserKeywordPatchDto]
     *
     * @return 업데이트 성공 시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordIdDto,
        patch: UserKeywordPatchDto,
    ): Boolean

    /**
     * 사용자별 키워드 ID로 사용자별 키워드를 삭제한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID DTO
     *
     * @return 삭제 성공시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordIdDto): Boolean
}
