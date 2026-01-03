package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

@Serializable
data class UserKeywordsResponse(val keywords: List<UserKeywordResponse>) {
    companion object {
        val sample = UserKeywordsResponse(listOf(UserKeywordResponse.sample))
    }
}

fun List<UserKeywordDto>.toResponse() =
    UserKeywordsResponse(keywords = this.map { it.toResponse() })
