package com.turnin.domain.pingPong.application.dto

import com.turnin.common.model.id.UserId

/**
 * 신고 대상 핑퐁 콘텐츠(질문/답변) DTO
 *
 * @property authorId 작성자 ID (질문: 질문자, 답변: 게시물 작성자)
 * @property content 질문/답변 내용
 */
data class ReportablePingPongContentDto(
    val authorId: UserId,
    val content: String,
)
