package com.turnin.domain.contentReport.exception

import com.turnin.common.exception.ApiErrorCode

/**
 * 콘텐츠 신고 커스텀 에러 코드
 */
sealed class ContentReportErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 신고할 콘텐츠를 찾을 수 없는 경우 */
    data object ContentNotFound : ContentReportErrorCode(CR001, "신고할 콘텐츠를 찾을 수 없습니다.")

    /** 본인이 작성한 콘텐츠를 신고하려는 경우 */
    data object CannotReportOwnContent : ContentReportErrorCode(CR002, "본인이 작성한 콘텐츠는 신고할 수 없습니다.")

    /** 이미 신고한 콘텐츠를 다시 신고하려는 경우 */
    data object AlreadyReported : ContentReportErrorCode(CR003, "이미 신고한 콘텐츠입니다.")

    /** 존재하지 않는 신고 사유로 신고하려는 경우 */
    data object InvalidReportReason : ContentReportErrorCode(CR004, "올바르지 않은 신고 사유입니다.")
}

private const val CR001 = "CR001"
private const val CR002 = "CR002"
private const val CR003 = "CR003"
private const val CR004 = "CR004"
