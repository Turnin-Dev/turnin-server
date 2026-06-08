package com.turnin.domain.announcement.presentation.dto

import com.turnin.common.model.AnnouncementStatus
import kotlinx.serialization.Serializable

/**
 * 공지 상태 변경 요청 바디
 *
 * @property status 공지 상태
 */
@Serializable
data class UpdateAnnouncementStatusRequest(val status: AnnouncementStatus) {
    companion object {
        val sample = UpdateAnnouncementStatusRequest(status = AnnouncementStatus.ACTIVE)
    }
}
