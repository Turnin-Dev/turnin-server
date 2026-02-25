package com.peekr.domain.userKeyword.infrastructure.provider

import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.application.provider.ReportProviderApi
import com.peekr.domain.userKeyword.domain.provider.ReportProvider

class ReportProviderImpl(private val reportProviderApi: ReportProviderApi) : ReportProvider {
    override suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean =
        reportProviderApi.existsByUserKeywordId(userKeywordId)
}
