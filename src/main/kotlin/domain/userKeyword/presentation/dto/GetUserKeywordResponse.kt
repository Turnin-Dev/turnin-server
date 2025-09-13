package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

@Serializable
data class GetUserKeywordResponse(val keywords: List<UserKeywordResponse>) {
    companion object {
        val sample = GetUserKeywordResponse(listOf(UserKeywordResponse.sample))
    }
}

fun List<UserKeywordDto>.toResponse() =
    GetUserKeywordResponse(keywords = this.map { it.toResponse() })
