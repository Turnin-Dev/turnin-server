package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordIdDto
import com.peekr.domain.keyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.keyword.application.dto.toDomain
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.service.UserKeywordService

class UserKeywordUseCaseImpl(private val userKeywordService: UserKeywordService) : UserKeywordUseCase {
    override suspend fun create(createUserKeywordDto: CreateUserKeywordDto): UserKeywordDto = userKeywordService
        .addUserKeyword(
            createUserKeywordDto.keyword,
            createUserKeywordDto.userId,
            createUserKeywordDto.offsetX,
            createUserKeywordDto.offsetY,
            createUserKeywordDto.description,
        ).toDto()

    override suspend fun getListById(userId: UserId): List<UserKeywordDto> =
        userKeywordService.getListById(userId).toDto()

    override suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordIdDto,
        patch: UserKeywordPatchDto,
    ): Boolean = userKeywordService.updateUserKeyword(
        ownerId,
        userKeywordId.toDomain(),
        patch.toDomain(),
    )

    override suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordIdDto): Boolean =
        userKeywordService.delete(ownerId, userKeywordId.toDomain())
}
