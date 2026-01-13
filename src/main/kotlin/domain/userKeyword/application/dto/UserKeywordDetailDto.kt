package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.UserInfo
import com.peekr.domain.userKeyword.domain.model.UserKeywordDetail

/**
 * 사용자 키워드 상세 정보 DTO
 *
 * 상세 정보에는 키워드 정보와 사용자 정보 일부가 함께 있다.
 *
 * @property userKeywordId 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용
 * @property userInfo 사용자 정보 일부 [UserInfo]
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeywordDetailDto(
    val userKeywordId: Long,
    val keywordId: Long,
    val keywordName: String,
    val description: String,
    val userInfo: UserInfoDto? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeywordDetail.toDto(): UserKeywordDetailDto =
    UserKeywordDetailDto(
        userKeywordId = userKeywordId.value,
        keywordId = keywordId.value,
        keywordName = keywordName.value,
        description = description.value ?: "",
        userInfo = userInfo?.toDto(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
