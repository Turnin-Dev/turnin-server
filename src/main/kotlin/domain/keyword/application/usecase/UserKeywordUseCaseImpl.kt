package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.common.model.UserId
import com.peekr.domain.keyword.application.dto.UserKeywordDto
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.UserKeywordPatch
import com.peekr.domain.keyword.domain.service.UserKeywordService

class UserKeywordUseCaseImpl(private val userKeywordService: UserKeywordService) : UserKeywordUseCase {
    override suspend fun add(userKeywordDto: UserKeywordDto): UserKeyword = userKeywordService.addUserKeyword(
        userKeywordDto.keyword,
        userKeywordDto.userId,
        userKeywordDto.offsetX,
        userKeywordDto.offsetY,
        userKeywordDto.description,
    )

    override suspend fun findById(userId: Long): List<UserKeyword> {
        val userId = UserId(userId)
        return userKeywordService.findUserKeywordById(userId)
    }

    override suspend fun update(
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = userKeywordService.updateUserKeyword(userKeywordId, patch)

    override suspend fun delete(userKeywordId: UserKeywordId): Boolean =
        userKeywordService.delete(userKeywordId)
}
