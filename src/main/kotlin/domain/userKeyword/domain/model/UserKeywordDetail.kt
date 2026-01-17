package com.peekr.domain.userKeyword.domain.model

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserKeywordId

/**
 * 사용자 키워드 상세 정보
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
data class UserKeywordDetail(
    val userKeywordId: UserKeywordId,
    val keywordId: KeywordId,
    val keywordName: KeywordName,
    val description: Description,
    val userInfo: UserInfo,
    val createdAt: Long,
    val updatedAt: Long,
)
