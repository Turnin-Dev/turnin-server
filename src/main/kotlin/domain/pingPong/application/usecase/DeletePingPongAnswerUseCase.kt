package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException

/**
 * 핑퐁 답변 삭제
 *
 * 질문이 달린 게시물(사용자 키워드)의 작성자(답변자)가 본인 답변을 삭제한다. 질문은 유지된다.
 *
 * @throws [ValidatorException] 핑퐁 ID가 0 이하인 경우
 * @throws [PingPongException.PingPongNotFound] 핑퐁(질문)이 없거나, 신고 누적으로 숨김 처리된 경우
 * @throws [PingPongException.NoPermissionToDelete] 게시물 작성자가 아닌 경우
 * @throws [PingPongException.PingPongAnswerNotFound] 답변이 없거나, 신고 누적으로 숨김 처리된 경우
 */
class DeletePingPongAnswerUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
) {
    /**
     * @param requesterId 삭제 요청자 ID
     * @param pingPongId 삭제할 답변이 달린 핑퐁(질문) ID
     */
    suspend operator fun invoke(
        requesterId: UserId,
        pingPongId: Long,
    ) {
        val pingPongIdVO = PingPongId(pingPongId)

        val pingPong = pingPongRepository.findVisibleById(pingPongIdVO)
            ?: throw PingPongException.PingPongNotFound()
        if (userKeywordProvider.findOwnerId(requesterId, pingPong.userKeywordId) != requesterId) {
            throw PingPongException.NoPermissionToDelete()
        }
        val answer = pingPongRepository.findVisibleAnswerByPingPongId(pingPongIdVO)
            ?: throw PingPongException.PingPongAnswerNotFound()

        // 질문 ID가 아닌 답변 ID로 삭제하여, 조회 이후 재등록된 답변이 삭제되지 않도록 한다.
        pingPongRepository.deleteAnswer(answer.id)
    }
}
