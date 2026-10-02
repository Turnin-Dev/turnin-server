package com.turnin.domain.pingPong.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 핑퐁 답변 작성 요청 바디
 *
 * @property answer 답변 내용
 */
@Serializable
data class CreatePingPongAnswerRequest(
    val answer: String,
) {
    companion object {
        val sample = CreatePingPongAnswerRequest(
            answer = "요즘 가장 자주 떠올리는 단어라서 골랐어요!",
        )
    }
}
