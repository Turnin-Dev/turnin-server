package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.UserId

/**
 * 답변 작성 가능 여부 판단에 필요한 정보
 *
 * @property pingPong 답변을 등록할 질문
 * @property ownerId 질문이 달린 게시물(사용자 키워드)의 작성자 ID.
 * 게시물 또는 작성자가 비활성 상태이거나, 답변자와 작성자가 차단 관계(양방향)라면 `null`
 * @property isQuestionerBlocked 답변자와 질문자가 차단 관계(양방향)인지 여부
 */
data class PingPongAnswerCreationTarget(
    val pingPong: PingPong,
    val ownerId: UserId?,
    val isQuestionerBlocked: Boolean,
)
