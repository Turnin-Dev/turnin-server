package com.turnin.domain.announcement.exception

import com.turnin.common.exception.ApiErrorCode

sealed class AnnouncementErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 공지를 찾을 수 없는 경우 */
    data object NotFound : AnnouncementErrorCode(ANN001, "공지를 찾을 수 없습니다.")
}

private const val ANN001 = "ANN001"
