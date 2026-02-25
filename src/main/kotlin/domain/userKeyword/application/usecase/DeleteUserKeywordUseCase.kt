package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.provider.ReportProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 키워드 삭제
 *
 * @see invoke
 */
class DeleteUserKeywordUseCase(
    private val userKeywordRepository: UserKeywordRepository,
    private val reportProvider: ReportProvider,
) {
    /**
     * 사용자 키워드 ID로 사용자 키워드를 삭제한다.
     *
     * ##### 정책 상 해당 사용자 키워드 ID가 신고 내역에 존재하면 Soft Delete를 수행해야 한다.
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID DTO
     *
     * @return 삭제 성공시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(ownerId: UserId, userKeywordId: UserKeywordId): Boolean {
        val isReported = reportProvider.existsByUserKeywordId(userKeywordId)

        return if (isReported) {
            // Soft Delete
            userKeywordRepository.deactivate(ownerId, userKeywordId)
        } else {
            // Hard Delete
            userKeywordRepository.delete(ownerId, userKeywordId)
        }
    }
}
