package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException

/**
 * 질문 삭제
 *
 * 질문자 본인 또는 질문이 달린 게시물(사용자 키워드)의 작성자가 질문을 삭제한다. 연결된 답변도 함께 삭제된다.
 *
 * 차단 관계여도 차단 전에 달린 질문은 각 사용자가 직접 삭제할 수 있다.
 *
 * @throws [ValidatorException] 핑퐁 ID가 0 이하인 경우
 * @throws [PingPongException.PingPongNotFound] 핑퐁이 없거나, 질문이 신고 누적으로 숨김 처리된 경우
 * @throws [PingPongException.NoPermissionToDelete] 질문자도 게시물 작성자도 아닌 경우
 */
class DeletePingPongQuestionUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
) {
    /**
     * @param requesterId 삭제 요청자 ID
     * @param pingPongId 삭제할 질문의 핑퐁 ID
     */
    suspend operator fun invoke(
        requesterId: UserId,
        pingPongId: Long,
    ) {
        val pingPongIdVO = PingPongId(pingPongId)

        val pingPong = pingPongRepository.findVisibleById(pingPongIdVO)
            ?: throw PingPongException.PingPongNotFound()
        if (pingPong.questionerId != requesterId &&
            userKeywordProvider.findOwnerId(requesterId, pingPong.userKeywordId) != requesterId
        ) {
            throw PingPongException.NoPermissionToDelete()
        }

        // 조회 이후 동시 삭제된 경우에도 결과(삭제됨)는 같으므로 삭제 여부는 확인하지 않는다.
        pingPongRepository.deleteQuestion(pingPongIdVO)
    }
}
