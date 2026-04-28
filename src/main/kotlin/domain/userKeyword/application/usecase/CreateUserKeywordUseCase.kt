package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.firebase.RefType
import com.turnin.common.model.NotificationType
import com.turnin.common.util.AppDispatchers
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.turnin.domain.userKeyword.application.dto.UserKeywordDto
import com.turnin.domain.userKeyword.application.dto.toDomain
import com.turnin.domain.userKeyword.application.dto.toDto
import com.turnin.domain.userKeyword.domain.message.UserKeywordNotificationMessage
import com.turnin.domain.userKeyword.domain.model.UserKeyword
import com.turnin.domain.userKeyword.domain.provider.FriendProvider
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider
import com.turnin.domain.userKeyword.domain.provider.NotificationProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.domain.userKeyword.exception.UserKeywordException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 사용자별 키워드를 추가한다.
 *
 * 등록할 키워드가 이미 존재한다면 해당 키워드의 ID로 저장하고,
 * 키워드가 존재하지 않는다면 키워드를 새롭게 등록 후 등록된 키워드의 ID로 저장한다.
 *
 * 키워드 체크와 생성 사이의 시간 간격 때문에 경쟁 조건이 발생할 수 있으므로, 원자적 처리 또는 예외 처리 수습이 필요하다.
 *
 * 해당 로직에서는 원자적 처리로 사용했다.
 */
class CreateUserKeywordUseCase(
    private val userKeywordRepository: UserKeywordRepository,
    private val keywordProvider: KeywordProvider,
    private val notificationProvider: NotificationProvider,
    private val friendProvider: FriendProvider,
    private val applicationScope: CoroutineScope,
) {
    /**
     * @param createUserKeywordDto [CreateUserKeywordDto] 사용자별 키워드 DTO
     *
     * @return [UserKeywordDto] 사용자별 키워드 DTO
     */
    suspend operator fun invoke(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto {
        val userKeyword = suspendTransaction {
            // 1) 사용자 키워드 개수 제한 확인
            val userKeywordCount = userKeywordRepository.countByUserId(createUserKeywordDto.userId)
            if (userKeywordCount >= UserKeyword.COUNT_LIMIT) {
                LOGGER.error("user keyword count exceed: userId=${createUserKeywordDto.userId}")
                throw UserKeywordException.CountLimitReached()
            }

            // 2) 키워드가 기존에 존재하는지 확인하고 없으면 생성 후 키워드 ID를 반환한다.
            val keyword = keywordProvider.findByName(createUserKeywordDto.keywordName)
                ?: keywordProvider.create(
                    keywordName = createUserKeywordDto.keywordName,
                    createdBy = createUserKeywordDto.userId,
                )

            // 3) 사용자 키워드 생성
            userKeywordRepository
                .create(
                    keyword.id,
                    createUserKeywordDto.userId,
                    createUserKeywordDto.description.toDomain(),
                ).toDto(keyword.name.value)
        }

        // 4) 친구들에게 새 키워드 알림 전송 (비동기 - 사용자 응답과 무관)
        //    알림 전송 실패 시에도 키워드 생성은 성공으로 처리
        applicationScope.launch(AppDispatchers.ioDispatcher) {
            runCatching {
                val fcmContext = friendProvider.getFriendFcmContext(createUserKeywordDto.userId)
                if (fcmContext.friendTokens.isNotEmpty()) {
                    notificationProvider.sendNotificationToTokens(
                        tokens = fcmContext.friendTokens,
                        notiType = NotificationType.NEW_KEYWORD,
                        title = UserKeywordNotificationMessage.TITLE,
                        message = UserKeywordNotificationMessage.message(fcmContext.senderName),
                        refId = userKeyword.id,
                        refType = RefType.KEYWORD,
                        senderUserId = createUserKeywordDto.userId.value,
                    )
                }
            }.onFailure { e ->
                if (e is CancellationException) throw e
                LOGGER.warn("새 키워드 알림 전송 실패 | userId=${createUserKeywordDto.userId}", e)
            }
        }

        return userKeyword
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateUserKeywordUseCase>()
