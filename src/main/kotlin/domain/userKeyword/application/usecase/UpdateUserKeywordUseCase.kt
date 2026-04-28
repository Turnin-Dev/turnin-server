package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.turnin.domain.userKeyword.application.dto.toDomain
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.domain.userKeyword.exception.UserKeywordException

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
     * @param ownerId 사용자 ID
     * @param patch [UserKeywordPatchDto] 사용자 키워드 수정 DTO
     *
     * @throws UserKeywordException.UpdateFailed 업데이트에 실패한 경우 예외가 발생한다.
     */
    suspend operator fun invoke(
        ownerId: Long,
        patch: UserKeywordPatchDto,
    ): Unit = suspendTransaction {
        // 0) 데이터 준비
        val ownerIdVO = UserId(ownerId)

        // 1) 키워드가 기존에 존재하는지 확인하고 없으면 생성 후 키워드 ID를 반환한다.
        val keyword = keywordProvider.findByName(patch.keywordName)
            ?: keywordProvider.create(
                keywordName = patch.keywordName,
                createdBy = ownerIdVO,
            )

        // 2) 사용자 키워드 업데이트
        val userKeywordPatch = patch.toDomain(keywordId = keyword.id)
        val updated = userKeywordRepository.update(ownerIdVO, userKeywordPatch)

        // 3) 결과 반환 및 실패 시 예외 발생(롤백 수행)
        if (!updated) {
            throw UserKeywordException.UpdateFailed()
        }
    }
}
