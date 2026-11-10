package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.UserId

/**
 * 답변 삭제 가능 여부 판단에 필요한 정보
 *
 * @property ownerId 질문이 달린 게시물(사용자 키워드)의 작성자 ID. 게시물 또는 작성자가 비활성 상태라면 `null`
 * @property answerId 노출 중인 답변 ID. 답변이 없거나 신고 누적으로 숨김 처리되었다면 `null`
 */
data class PingPongAnswerDeletionTarget(
    val ownerId: UserId?,
    val answerId: PingPongAnswerId?,
)
