package com.peekr.domain.userKeyword.infrastructure.service.impl

import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.domain.service.UserKeywordService

class UserKeywordServiceImpl(
    private val keywordRepository: KeywordRepository,
    private val userKeywordRepository: UserKeywordRepository,
) : UserKeywordService {
    override suspend fun findByUserId(userId: UserId): List<UserKeyword> =
        userKeywordRepository.getListById(userId)

    override suspend fun create(
        keyword: String,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword {
        val currentKeyword = keywordRepository.findByKeyword(keyword)
        return if (currentKeyword != null) {
            userKeywordRepository.save(currentKeyword.id, userId, offsetX, offsetY, description)
        } else {
            val newKeyword = keywordRepository.create(keyword, userId)
            userKeywordRepository.save(newKeyword.id, userId, offsetX, offsetY, description)
        }
    }

    override suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = userKeywordRepository.update(ownerId, userKeywordId, patch)

    override suspend fun delete(ownerId: UserId, userKeywordId: UserKeywordId): Boolean =
        userKeywordRepository.delete(ownerId, userKeywordId)
}
