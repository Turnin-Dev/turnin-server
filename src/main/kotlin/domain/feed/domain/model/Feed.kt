package com.peekr.domain.feed.domain.model

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description

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
 * @property score 피드 점수(피드 표시 조건을 위한 점수, 높을수록 피드가 표시될 확률이 높음)
 * @property similarity 유사도(사용자의 키워드들과 유사한 정도를 나타냄, 1.0에 가까울수록 유사함)
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
    val score: Double,
    val similarity: Double,
)
