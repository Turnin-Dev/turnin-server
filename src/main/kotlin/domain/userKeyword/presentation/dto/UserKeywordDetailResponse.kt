package com.turnin.domain.userKeyword.presentation.dto

import com.turnin.domain.userKeyword.application.dto.UserKeywordDetailDto
import com.turnin.domain.userKeyword.domain.model.UserInfo
import kotlinx.serialization.Serializable

/**
 * 사용자 키워드 상세 정보 응답 바디
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
@Serializable
data class UserKeywordDetailResponse(
    val userKeywordId: Long,
    val keywordId: Long,
    val keywordName: String,
    val description: String,
    val userInfo: UserInfoResponse,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = UserKeywordDetailResponse(
            userKeywordId = 1L,
            keywordId = 1L,
            keywordName = "keyword",
            description = "description",
            userInfo = UserInfoResponse(
                userId = 1L,
                userName = "user",
                profileImageUrl = "https://www.example.com/image.jpg",
            ),
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun UserKeywordDetailDto.toResponse(): UserKeywordDetailResponse =
    UserKeywordDetailResponse(
        userKeywordId = userKeywordId,
        keywordId = keywordId,
        keywordName = keywordName,
        description = description,
        userInfo = userInfo.toResponse(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
