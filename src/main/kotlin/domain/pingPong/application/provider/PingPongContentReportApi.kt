package com.turnin.domain.pingPong.application.provider

import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.domain.pingPong.application.dto.ReportablePingPongContentDto
import com.turnin.domain.pingPong.domain.repository.PingPongRepository

/**
 * 외부(콘텐츠 신고)에 제공할 PingPong API
 */
class PingPongContentReportApi(private val pingPongRepository: PingPongRepository) {
    /**
     * 노출 중인 질문을 신고 대상 정보로 조회한다.
     *
     * @param pingPongId 핑퐁 ID
     *
     * @return 노출 중인 질문이 있다면 [ReportablePingPongContentDto]를 반환하고, 없거나 숨김 처리되었다면 `null`을 반환한다.
     */
    suspend fun findVisibleQuestion(pingPongId: PingPongId): ReportablePingPongContentDto? =
        pingPongRepository.findVisibleById(pingPongId)?.let {
            ReportablePingPongContentDto(authorId = it.questionerId, content = it.question.value)
        }

    /**
     * 노출 중인 답변을 신고 대상 정보로 조회한다.
     *
     * @param answerId 답변 ID
     *
     * @return 노출 중인 답변이 있다면 [ReportablePingPongContentDto]를 반환하고, 답변 또는 질문이 없거나 숨김 처리되었다면 `null`을 반환한다.
     */
    suspend fun findVisibleAnswer(answerId: PingPongAnswerId): ReportablePingPongContentDto? =
        pingPongRepository.findVisibleAnswerWithAnswererById(answerId)?.let {
            ReportablePingPongContentDto(authorId = it.answererId, content = it.answer.answer.value)
        }

    /**
     * 질문을 숨김 처리한다.
     *
     * @param pingPongId 핑퐁 ID
     */
    suspend fun hideQuestion(pingPongId: PingPongId) {
        pingPongRepository.hideQuestion(pingPongId)
    }

    /**
     * 답변을 숨김 처리한다.
     *
     * @param answerId 답변 ID
     */
    suspend fun hideAnswer(answerId: PingPongAnswerId) {
        pingPongRepository.hideAnswer(answerId)
    }
}
