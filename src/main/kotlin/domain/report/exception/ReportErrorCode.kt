package com.peekr.domain.report.exception

import com.peekr.common.exception.ApiErrorCode

sealed class ReportErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 필수 신고 대상이 빠져있는 경우 */
    data object MissingReportTarget : ReportErrorCode(RP001, "필수 신고 대상이 빠져있습니다.")
}

private const val RP001 = "RP001"
