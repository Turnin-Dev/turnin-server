package com.turnin.domain.contentReport.di

import com.turnin.domain.contentReport.application.usecase.ContentReportUseCases
import com.turnin.domain.contentReport.application.usecase.CreateContentReportUseCase
import com.turnin.domain.contentReport.domain.provider.ReportableContentProvider
import com.turnin.domain.contentReport.domain.repository.ContentReportRepository
import com.turnin.domain.contentReport.infrastructure.provider.ReportableContentProviderImpl
import com.turnin.domain.contentReport.infrastructure.repository.ContentReportRepositoryImpl
import org.koin.dsl.module

val contentReportModule = module {
    // Repository
    single<ContentReportRepository> { ContentReportRepositoryImpl() }

    // Provider
    single<ReportableContentProvider> { ReportableContentProviderImpl(get()) }

    // UseCases
    single { CreateContentReportUseCase(get(), get()) }
    single { ContentReportUseCases(get()) }
}
