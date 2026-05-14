package com.turnin.domain.report.exception

import com.turnin.common.exception.ApiErrorCode

sealed class ReportErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 필수 신고 대상이 빠져있는 경우 */
    data object MissingReportTarget : ReportErrorCode(RP001, "필수 신고 대상이 빠져있습니다.")

    /** 자기 자신을 신고할 수 없습니다. */
    data object CannotReportMySelf : ReportErrorCode(RP002, "본인을 신고할 수 없습니다.")
}

private const val RP001 = "RP001"
private const val RP002 = "RP002"
