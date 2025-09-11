package com.peekr.domain.userKeyword.infrastructure.service.impl

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.domain.service.UserKeywordService

class UserKeywordServiceImpl(private val userKeywordRepository: UserKeywordRepository) : UserKeywordService {
    override suspend fun getKeywords(userId: UserId): List<UserKeyword> =
        userKeywordRepository.findByUserId(userId)

    override suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword =
        userKeywordRepository.create(keywordId, userId, offsetX, offsetY, description)

    override suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = userKeywordRepository.update(ownerId, userKeywordId, patch)

    override suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordId): Boolean =
        userKeywordRepository.delete(ownerId, userKeywordId)
}
