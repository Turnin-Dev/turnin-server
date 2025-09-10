package com.peekr.domain.keyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

@Serializable
data class GetUserKeywordResponse(val keywords: List<UserKeywordResponse>)

fun List<UserKeywordDto>.toResponse() =
    GetUserKeywordResponse(keywords = this.map { it.toResponse() })
