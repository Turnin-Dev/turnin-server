package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.firebase.RefDataKey
import com.turnin.common.firebase.RefType
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.application.dto.toDto
import com.turnin.domain.pingPong.domain.message.PingPongNotificationMessage
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongNotificationCommand
import com.turnin.domain.pingPong.domain.provider.NotificationProvider
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.provider.UserProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 질문 작성
 *
 * 게시물(사용자 키워드)에 질문을 등록한다.
 *
 * 연속 작성 제한은 유스케이스가 아닌 라우트의 RateLimit으로 처리한다.
 *
 * 질문 등록 후 게시물 작성자에게 질문 알림을 비동기로 전송한다. (알림 전송 실패 시에도 질문 등록은 성공으로 처리)
 *
 * @throws [ValidatorException] 질문 내용이 비어있거나 최대 글자 수를 초과한 경우
 * @throws [PingPongException.UserKeywordNotFound] 게시물이 없거나, 비활성화/차단 관계로 조회할 수 없는 경우
 * @throws [PingPongException.CannotQuestionOwnUserKeyword] 본인 게시물에 질문을 등록하려는 경우
 */
class CreatePingPongQuestionUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
    private val userProvider: UserProvider,
    private val notificationProvider: NotificationProvider,
    private val applicationScope: CoroutineScope,
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

        val pingPong = pingPongRepository.createQuestion(userKeywordIdVO, questionerId, questionVO)

        // 게시물 작성자에게 질문 알림 전송 (비동기 - 사용자 응답과 무관)
        applicationScope.launch {
            runCatching {
                val questionerName = userProvider.findUserName(questionerId)
                    ?: return@launch
                notificationProvider.sendNotification(
                    PingPongNotificationCommand(
                        receiverId = ownerId,
                        notiType = NotificationType.PING_PONG_QUESTION,
                        title = PingPongNotificationMessage.Question.TITLE,
                        message = PingPongNotificationMessage.Question.message(questionerName.value),
                        refId = userKeywordIdVO.value,
                        refType = RefType.KEYWORD,
                        refData = mapOf(RefDataKey.REF_OWNER_ID to ownerId.value.toString()),
                    ),
                )
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("핑퐁 질문 알림 전송 실패 | pingPongId=${pingPong.id.value}", e)
            }
        }

        return pingPong.toDto()
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreatePingPongQuestionUseCase>()
