package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 키워드 업데이트
 *
 * @see invoke
 */
class UpdateUserKeywordUseCase(
    private val userKeywordRepository: UserKeywordRepository,
    private val keywordProvider: KeywordProvider,
) {
    /**
     * 사용자 키워드를 업데이트한다.
     *
     * @param patch [UserKeywordPatchDto] 사용자 키워드 수정 DTO
     *
     * @return 업데이트 성공 시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(patch: UserKeywordPatchDto): Boolean {
        // 1) 키워드가 기존에 존재하는지 확인하고 없으면 생성 후 키워드 ID를 반환한다.
        val keyword = keywordProvider.findByName(patch.keywordName)
            ?: keywordProvider.create(
                keywordName = patch.keywordName,
                createdBy = UserId(patch.ownerId),
            )

        // 2) 사용자 키워드 생성
        val userKeywordPatch = patch.toDomain(keywordId = keyword.id)

        return userKeywordRepository.update(userKeywordPatch)
    }
}
