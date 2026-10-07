package com.turnin.domain.pingPong.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 질문 작성 요청 바디
 *
 * @property question 질문 내용
 */
@Serializable
data class CreatePingPongQuestionRequest(
    val question: String,
) {
    companion object {
        val sample = CreatePingPongQuestionRequest(
            question = "이 키워드를 고른 이유가 궁금해요!",
        )
    }
}
