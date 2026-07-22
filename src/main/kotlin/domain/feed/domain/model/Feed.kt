package com.turnin.domain.feed.domain.model

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description

/**
 * 피드 모델 클래스
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 프로필 사진 URL
 * @property keywordId 키워드 ID
 * @property keyword 키워드 명
 * @property description 키워드 내용
 * @property createdAt 키워드 생성 일자
 */
data class Feed(
    val userKeywordId: UserKeywordId,
    val userId: UserId,
    val userName: UserName,
    val profileImageUrl: String?,
    val keywordId: KeywordId,
    val keyword: KeywordName,
    val description: Description,
    val createdAt: Long,
)
