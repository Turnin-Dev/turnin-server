package com.peekr.domain.keyword.infrastructure.service.impl

import com.peekr.domain.common.model.UserId
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.UserKeywordPatch
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.domain.repository.UserKeywordRepository
import com.peekr.domain.keyword.domain.service.UserKeywordService

class UserKeywordServiceImpl(
    private val keywordRepository: KeywordRepository,
    private val userKeywordRepository: UserKeywordRepository,
) : UserKeywordService {
    override suspend fun findUserKeywordById(userId: UserId): List<UserKeyword> =
        userKeywordRepository.findByUserId(userId)

    override suspend fun addUserKeyword(
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
            val newKeyword = keywordRepository.save(keyword, userId)
            userKeywordRepository.save(newKeyword.id, userId, offsetX, offsetY, description)
        }
    }

    override suspend fun updateUserKeyword(
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = userKeywordRepository.update(userKeywordId, patch)

    override suspend fun delete(userKeywordId: UserKeywordId): Boolean =
        userKeywordRepository.delete(userKeywordId)
}
