package com.turnin.domain.keyword.domain.model

import com.turnin.common.ml.keywordCategory.KeywordCategory
import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId

/**
 * 키워드
 *
 * @property id 키워드 ID
 * @property name 키워드명
 * @property embedding 키워드 임베딩
 * @property category 키워드 카테고리
 * @property categorySimilarity 키워드 - 카테고리 유사도
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class Keyword(
    val id: KeywordId,
    val name: KeywordName,
    val embedding: String,
    val category: KeywordCategory?,
    val categorySimilarity: Double?,
    val createdBy: UserId?,
    val createdAt: Long,
    val updatedAt: Long,
)
