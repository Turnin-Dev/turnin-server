package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.application.dto.toDto
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException

/**
 * 질문 작성
 *
 * 게시물(사용자 키워드)에 질문을 등록한다.
 *
 * 연속 작성 제한은 유스케이스가 아닌 라우트의 RateLimit으로 처리한다.
 *
 * @throws [ValidatorException] 질문 내용이 비어있거나 최대 글자 수를 초과한 경우
 * @throws [PingPongException.UserKeywordNotFound] 게시물이 없거나, 비활성화/차단 관계로 조회할 수 없는 경우
 * @throws [PingPongException.CannotQuestionOwnUserKeyword] 본인 게시물에 질문을 등록하려는 경우
 */
class CreatePingPongQuestionUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
) {
    /**
     * @param questionerId 질문자(요청자) ID
     * @param userKeywordId 질문을 등록할 사용자 키워드(게시물) ID
     * @param question 질문 내용
     *
     * @return 생성된 [PingPongDto]
     */
    suspend operator fun invoke(
        questionerId: UserId,
        userKeywordId: Long,
        question: String,
    ): PingPongDto {
        val userKeywordIdVO = UserKeywordId(userKeywordId)
        val questionVO = PingPongContent(question)

        val ownerId = userKeywordProvider.findOwnerId(questionerId, userKeywordIdVO)
            ?: throw PingPongException.UserKeywordNotFound()
        if (ownerId == questionerId) {
            throw PingPongException.CannotQuestionOwnUserKeyword()
        }

        return pingPongRepository
            .createQuestion(userKeywordIdVO, questionerId, questionVO)
            .toDto()
    }
}
