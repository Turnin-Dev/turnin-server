package com.turnin.domain.pingPong.application.dto

import com.turnin.domain.pingPong.domain.model.PingPong

/**
 * 핑퐁 DTO
 *
 * @property id 핑퐁 ID
 * @property userKeywordId 질문이 달린 사용자 키워드(게시물) ID
 * @property questionerId 질문자 ID
 * @property question 질문 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class PingPongDto(
    val id: Long,
    val userKeywordId: Long,
    val questionerId: Long,
    val question: String,
    val createdAt: Long,
    val updatedAt: Long,
)

fun PingPong.toDto(): PingPongDto = PingPongDto(
    id = id.value,
    userKeywordId = userKeywordId.value,
    questionerId = questionerId.value,
    question = question.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
