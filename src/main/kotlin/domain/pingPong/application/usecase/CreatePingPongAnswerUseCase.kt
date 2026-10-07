package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.firebase.RefDataKey
import com.turnin.common.firebase.RefType
import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.pingPong.application.dto.PingPongAnswerDto
import com.turnin.domain.pingPong.application.dto.toDto
import com.turnin.domain.pingPong.domain.message.PingPongNotificationMessage
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongNotificationCommand
import com.turnin.domain.pingPong.domain.provider.BlockProvider
import com.turnin.domain.pingPong.domain.provider.NotificationProvider
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.provider.UserProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 답변 작성
 *
 * 질문에 답변을 등록한다. 답변은 질문이 달린 게시물(사용자 키워드)의 작성자만 등록할 수 있으며, 질문당 1개만 등록할 수 있다.
 *
 * 답변 등록 후 질문자에게 답변 알림을 비동기로 전송한다. (알림 전송 실패 시에도 답변 등록은 성공으로 처리)
 *
 * @throws [ValidatorException] 답변 내용이 비어있거나 최대 글자 수를 초과한 경우
 * @throws [PingPongException.PingPongNotFound] 핑퐁이 없거나, 질문이 신고 누적으로 숨김 처리된 경우
 * @throws [PingPongException.UserKeywordNotFound] 질문이 달린 게시물이 없거나, 비활성화/차단 관계로 조회할 수 없는 경우
 * 차단 전에 달린 질문은 작성자에게 노출되지만(삭제/신고용), 차단 관계(양방향)인 질문자의 질문에는 답변할 수 없다.
 *
 * @throws [PingPongException.NotUserKeywordOwner] 게시물 작성자가 아닌 사용자가 답변을 등록하려는 경우
 * @throws [PingPongException.CannotAnswerBlockedQuestioner] 질문자와 차단 관계(양방향)인 경우
 * @throws [PingPongException.AlreadyAnswered] 이미 답변이 등록된 질문인 경우
 */
class CreatePingPongAnswerUseCase(
    private val pingPongRepository: PingPongRepository,
    private val userKeywordProvider: UserKeywordProvider,
    private val blockProvider: BlockProvider,
    private val userProvider: UserProvider,
    private val notificationProvider: NotificationProvider,
    private val applicationScope: CoroutineScope,
) {
    /**
     * @param answererId 답변자(요청자) ID
     * @param pingPongId 답변을 등록할 질문의 핑퐁 ID
     * @param answer 답변 내용
     *
     * @return 생성된 [PingPongAnswerDto]
     */
    suspend operator fun invoke(
        answererId: UserId,
        pingPongId: Long,
        answer: String,
    ): PingPongAnswerDto {
        val pingPongIdVO = PingPongId(pingPongId)
        val answerVO = PingPongContent(answer)

        val pingPong = pingPongRepository.findVisibleById(pingPongIdVO)
            ?: throw PingPongException.PingPongNotFound()
        val ownerId = userKeywordProvider.findOwnerId(answererId, pingPong.userKeywordId)
            ?: throw PingPongException.UserKeywordNotFound()
        if (ownerId != answererId) {
            throw PingPongException.NotUserKeywordOwner()
        }
        if (blockProvider.isBlockedRelationship(answererId, pingPong.questionerId)) {
            throw PingPongException.CannotAnswerBlockedQuestioner()
        }

        // 질문당 답변 1개는 DB 유니크 제약으로 보장한다. (동시 요청에도 안전)
        val pingPongAnswer = try {
            pingPongRepository.createAnswer(pingPongIdVO, answerVO)
        } catch (e: DatabaseException.DuplicatedDataException) {
            throw PingPongException.AlreadyAnswered(e)
        }

        // 질문자에게 답변 알림 전송 (비동기 - 사용자 응답과 무관)
        applicationScope.launch {
            runCatching {
                val answererName = userProvider.findUserName(answererId)
                    ?: return@launch
                notificationProvider.sendNotification(
                    PingPongNotificationCommand(
                        receiverId = pingPong.questionerId,
                        notiType = NotificationType.PING_PONG_ANSWER,
                        title = PingPongNotificationMessage.Answer.TITLE,
                        message = PingPongNotificationMessage.Answer.message(answererName.value),
                        refId = pingPong.userKeywordId.value,
                        refType = RefType.KEYWORD,
                        refData = mapOf(RefDataKey.REF_OWNER_ID to answererId.value.toString()),
                    ),
                )
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("핑퐁 답변 알림 전송 실패 | pingPongId=${pingPongIdVO.value}", e)
            }
        }

        return pingPongAnswer.toDto()
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreatePingPongAnswerUseCase>()
