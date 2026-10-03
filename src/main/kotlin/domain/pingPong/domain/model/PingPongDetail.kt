package com.turnin.domain.pingPong.domain.model

/**
 * 핑퐁 상세 모델 (목록 조회용)
 *
 * @property pingPong 핑퐁(질문)
 * @property questioner 질문자 정보
 * @property answer 핑퐁 답변 (답변이 없거나 숨김 처리된 경우 `null`)
 */
data class PingPongDetail(
    val pingPong: PingPong,
    val questioner: PingPongQuestioner,
    val answer: PingPongAnswer?,
)
