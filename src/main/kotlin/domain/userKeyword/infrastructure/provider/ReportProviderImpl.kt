package com.turnin.domain.userKeyword.infrastructure.provider

import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.report.application.provider.ReportProviderApi
import com.turnin.domain.userKeyword.domain.provider.ReportProvider

class ReportProviderImpl(private val reportProviderApi: ReportProviderApi) : ReportProvider {
    override suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean =
        reportProviderApi.existsByUserKeywordId(userKeywordId)
}
