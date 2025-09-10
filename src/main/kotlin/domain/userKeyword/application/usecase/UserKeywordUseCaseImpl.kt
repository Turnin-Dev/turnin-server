package com.peekr.domain.userKeyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordIdDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.service.UserKeywordService

class UserKeywordUseCaseImpl(private val userKeywordService: UserKeywordService) : UserKeywordUseCase {
    override suspend fun create(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto = userKeywordService
        .create(
            createUserKeywordDto.keyword,
            createUserKeywordDto.userId,
            createUserKeywordDto.offsetX,
            createUserKeywordDto.offsetY,
            createUserKeywordDto.description,
        ).toDto()

    override suspend fun getListById(userId: UserId): List<UserKeywordDto> =
        userKeywordService.findByUserId(userId).toDto()

    override suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordIdDto,
        patch: UserKeywordPatchDto,
    ): Boolean = userKeywordService.update(
        ownerId,
        userKeywordId.toDomain(),
        patch.toDomain(),
    )

    override suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordIdDto): Boolean =
        userKeywordService.delete(ownerId, userKeywordId.toDomain())
}
