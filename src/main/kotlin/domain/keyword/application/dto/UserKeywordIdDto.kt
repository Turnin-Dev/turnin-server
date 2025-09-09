package com.peekr.domain.keyword.application.dto

import com.peekr.domain.keyword.domain.model.UserKeywordId

/** 사용자별 키워드 ID 래퍼 클래스 DTO */
data class UserKeywordIdDto(val id: Long)

fun UserKeywordIdDto.toDomain(): UserKeywordId = UserKeywordId(this.id)
