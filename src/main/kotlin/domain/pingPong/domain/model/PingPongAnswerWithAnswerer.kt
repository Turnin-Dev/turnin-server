package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.UserId

/**
 * 답변자 정보를 포함한 핑퐁 답변 모델
 *
 * @property answer 핑퐁 답변
 * @property answererId 답변자 ID (질문이 달린 게시물(사용자 키워드)의 작성자)
 */
data class PingPongAnswerWithAnswerer(
    val answer: PingPongAnswer,
    val answererId: UserId,
)
