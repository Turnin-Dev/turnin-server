package com.turnin.domain.contentReport.domain.model

import com.turnin.common.model.id.UserId

/**
 * 신고 대상 콘텐츠 모델
 *
 * @property authorId 콘텐츠 작성자 ID (피신고자)
 * @property snapshot 신고 시점의 콘텐츠 내용
 */
data class ReportableContent(
    val authorId: UserId,
    val snapshot: String,
)
