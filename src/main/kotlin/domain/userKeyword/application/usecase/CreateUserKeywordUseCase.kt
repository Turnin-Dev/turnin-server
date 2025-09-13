package com.peekr.domain.userKeyword.application.usecase

import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.service.UserKeywordService

/**
 * 사용자별 키워드를 추가한다.
 */
class CreateUserKeywordUseCase(private val userKeywordService: UserKeywordService) {
    /**
     * @param createUserKeywordDto [CreateUserKeywordDto] 사용자별 키워드 DTO
     *
     * @return [UserKeywordDto] 사용자별 키워드 DTO
     */
    suspend operator fun invoke(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto = userKeywordService
        .create(
            createUserKeywordDto.keywordId,
            createUserKeywordDto.userId,
            createUserKeywordDto.offsetX,
            createUserKeywordDto.offsetY,
            createUserKeywordDto.description,
        ).toDto()
}
