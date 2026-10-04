package com.turnin.domain.contentReport.infrastructure.provider

import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.domain.contentReport.domain.model.ReportableContent
import com.turnin.domain.contentReport.domain.provider.ReportableContentProvider
import com.turnin.domain.pingPong.application.dto.ReportablePingPongContentDto
import com.turnin.domain.pingPong.application.provider.PingPongContentReportApi

class ReportableContentProviderImpl(
    private val pingPongContentReportApi: PingPongContentReportApi,
) : ReportableContentProvider {
    override suspend fun findVisible(
        contentType: ContentReportType,
        contentId: Long,
    ): ReportableContent? {
        val content = when (contentType) {
            ContentReportType.PING_PONG_QUESTION -> {
                pingPongContentReportApi.findVisibleQuestion(PingPongId(contentId))
            }
            ContentReportType.PING_PONG_ANSWER -> {
                pingPongContentReportApi.findVisibleAnswer(PingPongAnswerId(contentId))
            }
        }
        return content?.toReportableContent()
    }

    override suspend fun hide(
        contentType: ContentReportType,
        contentId: Long,
    ) {
        when (contentType) {
            ContentReportType.PING_PONG_QUESTION -> pingPongContentReportApi.hideQuestion(PingPongId(contentId))
            ContentReportType.PING_PONG_ANSWER -> pingPongContentReportApi.hideAnswer(PingPongAnswerId(contentId))
        }
    }

    private fun ReportablePingPongContentDto.toReportableContent(): ReportableContent =
        ReportableContent(authorId = authorId, snapshot = content)
}
