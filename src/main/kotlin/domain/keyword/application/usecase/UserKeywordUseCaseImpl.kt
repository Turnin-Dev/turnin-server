package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.AddUserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordDto
import com.peekr.domain.keyword.application.dto.UserKeywordIdDto
import com.peekr.domain.keyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.keyword.application.dto.toDomain
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.service.UserKeywordService

class UserKeywordUseCaseImpl(private val userKeywordService: UserKeywordService) : UserKeywordUseCase {
    override suspend fun add(addUserKeywordDto: AddUserKeywordDto): UserKeywordDto = userKeywordService
        .addUserKeyword(
            addUserKeywordDto.keyword,
            addUserKeywordDto.userId,
            addUserKeywordDto.offsetX,
            addUserKeywordDto.offsetY,
            addUserKeywordDto.description,
        ).toDto()

    override suspend fun getListById(userId: UserId): List<UserKeywordDto> =
        userKeywordService.getListById(userId).toDto()

    override suspend fun update(
        userKeywordId: UserKeywordIdDto,
        patch: UserKeywordPatchDto,
    ): Boolean = userKeywordService.updateUserKeyword(
        userKeywordId.toDomain(),
        patch.toDomain(),
    )

    override suspend fun delete(userKeywordId: UserKeywordIdDto): Boolean =
        userKeywordService.delete(userKeywordId.toDomain())
}
