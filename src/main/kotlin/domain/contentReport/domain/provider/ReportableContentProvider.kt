package com.turnin.domain.contentReport.domain.provider

import com.turnin.common.model.ContentReportType
import com.turnin.common.validator.ValidatorException
import com.turnin.domain.contentReport.domain.model.ReportableContent

/**
 * 외부에서 제공받은 신고 대상 콘텐츠 API
 *
 * [ContentReportType]에 따라 해당 콘텐츠를 소유한 도메인에 요청한다.
 */
interface ReportableContentProvider {
    /**
     * 노출 중인 신고 대상 콘텐츠를 조회한다.
     *
     * @param contentType 콘텐츠 유형
     * @param contentId 콘텐츠 ID
     *
     * @return 노출 중인 콘텐츠가 있다면 [ReportableContent]를 반환하고, 없거나 숨김 처리되었다면 `null`을 반환한다.
     *
     * @throws ValidatorException 콘텐츠 ID가 올바르지 않은 경우
     */
    suspend fun findVisible(
        contentType: ContentReportType,
        contentId: Long,
    ): ReportableContent?

    /**
     * 콘텐츠를 숨김 처리한다.
     *
     * @param contentType 콘텐츠 유형
     * @param contentId 콘텐츠 ID
     */
    suspend fun hide(
        contentType: ContentReportType,
        contentId: Long,
    )
}
