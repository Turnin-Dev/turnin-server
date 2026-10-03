package com.turnin.domain.pingPong.application.dto

import com.turnin.domain.pingPong.domain.model.PingPongDetail
import com.turnin.domain.pingPong.domain.model.PingPongQuestioner

/**
 * 핑퐁 상세 DTO (목록 조회용)
 *
 * @property pingPong 핑퐁(질문)
 * @property questioner 질문자 정보
 * @property answer 핑퐁 답변 (답변이 없거나 숨김 처리된 경우 `null`)
 */
data class PingPongDetailDto(
    val pingPong: PingPongDto,
    val questioner: PingPongQuestionerDto,
    val answer: PingPongAnswerDto?,
)

/**
 * 질문자 정보 DTO
 *
 * @property userId 질문자 ID
 * @property userName 질문자 이름
 * @property profileImageUrl 질문자 프로필 사진 url
 */
data class PingPongQuestionerDto(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
)

fun PingPongDetail.toDto(): PingPongDetailDto = PingPongDetailDto(
    pingPong = pingPong.toDto(),
    questioner = questioner.toDto(),
    answer = answer?.toDto(),
)

fun PingPongQuestioner.toDto(): PingPongQuestionerDto = PingPongQuestionerDto(
    userId = userId.value,
    userName = userName.value,
    profileImageUrl = profileImageUrl,
)
