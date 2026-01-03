package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException

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
) {
    /**
     * @param createUserKeywordDto [CreateUserKeywordDto] 사용자별 키워드 DTO
     *
     * @return [UserKeywordDto] 사용자별 키워드 DTO
     */
    suspend operator fun invoke(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto = suspendTransaction {
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
            ).toDto(keyword.name)
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateUserKeywordUseCase>()
