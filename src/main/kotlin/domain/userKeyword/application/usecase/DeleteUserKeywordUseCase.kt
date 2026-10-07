package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.provider.PingPongProvider
import com.turnin.domain.userKeyword.domain.provider.ReportProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자 키워드 삭제
 *
 * @see invoke
 */
class DeleteUserKeywordUseCase(
    private val userKeywordRepository: UserKeywordRepository,
    private val reportProvider: ReportProvider,
    private val pingPongProvider: PingPongProvider,
) {
    /**
     * 사용자 키워드 ID로 사용자 키워드를 삭제한다.
     *
     * ##### 정책 상 해당 사용자 키워드 ID가 신고 내역에 존재하면 Soft Delete(비활성화 + 삭제 시각 기록)를 수행해야 한다.
     * ##### Soft Delete 된 키워드는 삭제 시각 기준 1년 후 신고 내역과 함께 파기된다.
     *
     * 게시물에 달린 핑퐁은 신고 여부와 관계없이 삭제된다. (Hard Delete 시 CASCADE, Soft Delete 시 명시적 삭제)
     *
     * @param ownerId 사용자 ID
     * @param userKeywordId 사용자별 키워드 ID DTO
     *
     * @return 삭제 성공시 `true`를 반환하고 실패 시 `false`를 반환한다.
     */
    suspend operator fun invoke(ownerId: UserId, userKeywordId: UserKeywordId): Boolean = suspendTransaction {
        val isReported = reportProvider.existsByUserKeywordId(userKeywordId)

        if (isReported) {
            // Soft Delete (본인 게시물이 삭제 처리된 경우에만 핑퐁 삭제)
            userKeywordRepository.softDelete(ownerId, userKeywordId).also { isSoftDeleted ->
                if (isSoftDeleted) pingPongProvider.deleteAllByUserKeywordId(userKeywordId)
            }
        } else {
            // Hard Delete (핑퐁은 CASCADE로 함께 삭제)
            userKeywordRepository.delete(ownerId, userKeywordId)
        }
    }
}
