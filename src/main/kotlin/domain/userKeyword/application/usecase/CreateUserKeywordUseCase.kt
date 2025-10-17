package com.peekr.domain.userKeyword.application.usecase

import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.application.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 사용자별 키워드를 추가한다.
 *
 * 등록할 키워드가 이미 존재한다면 해당 키워드의 ID로 저장하고,
 * 키워드가 존재하지 않는다면 키워드를 새롭게 등록 후 등록된 키워드의 ID로 저장한다.
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
    suspend operator fun invoke(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto {
        val keyword = keywordProvider.findByName(createUserKeywordDto.keywordName)
        return if (keyword != null) {
            userKeywordRepository
                .create(
                    keyword.id,
                    createUserKeywordDto.userId,
                    createUserKeywordDto.offsetX,
                    createUserKeywordDto.offsetY,
                    createUserKeywordDto.description,
                ).toDto(keyword.keyword)
        } else {
            val savedKeyword = keywordProvider.create(
                keywordName = createUserKeywordDto.keywordName,
                createdBy = createUserKeywordDto.userId,
            )
            userKeywordRepository
                .create(
                    savedKeyword.id,
                    createUserKeywordDto.userId,
                    createUserKeywordDto.offsetX,
                    createUserKeywordDto.offsetY,
                    createUserKeywordDto.description,
                ).toDto(savedKeyword.keyword)
        }
    }
}
