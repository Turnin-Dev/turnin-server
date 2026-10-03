package com.turnin.domain.pingPong.presentation.dto

import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.pingPong.application.dto.PingPongDetailDto
import com.turnin.domain.pingPong.application.dto.PingPongQuestionerDto
import kotlinx.serialization.Serializable

/**
 * 핑퐁 상세 응답 바디 (목록 조회용)
 *
 * @property pingPong 핑퐁(질문)
 * @property questioner 질문자 정보
 * @property answer 핑퐁 답변 (답변이 없거나 숨김 처리된 경우 `null`)
 */
@Serializable
data class PingPongDetailResponse(
    val pingPong: PingPongResponse,
    val questioner: PingPongQuestionerResponse,
    val answer: PingPongAnswerResponse?,
) {
    companion object {
        val sample = CursorPage(
            items = listOf(
                PingPongDetailResponse(
                    pingPong = PingPongResponse.sample.copy(id = 2),
                    questioner = PingPongQuestionerResponse.sample,
                    answer = null,
                ),
                PingPongDetailResponse(
                    pingPong = PingPongResponse.sample,
                    questioner = PingPongQuestionerResponse.sample,
                    answer = PingPongAnswerResponse.sample,
                ),
            ),
            nextCursor = 1L,
        )
    }
}

/**
 * 질문자 정보 응답 바디
 *
 * @property userId 질문자 ID
 * @property userName 질문자 이름
 * @property profileImageUrl 질문자 프로필 사진 url
 */
@Serializable
data class PingPongQuestionerResponse(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
) {
    companion object {
        val sample = PingPongQuestionerResponse(
            userId = 2,
            userName = "홍길동",
            profileImageUrl = "https://example.com/profile.jpg",
        )
    }
}

fun PingPongDetailDto.toResponse() = PingPongDetailResponse(
    pingPong = pingPong.toResponse(),
    questioner = questioner.toResponse(),
    answer = answer?.toResponse(),
)

fun PingPongQuestionerDto.toResponse() = PingPongQuestionerResponse(
    userId = userId,
    userName = userName,
    profileImageUrl = profileImageUrl,
)
