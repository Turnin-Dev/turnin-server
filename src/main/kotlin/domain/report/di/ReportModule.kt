package com.turnin.domain.report.di

import com.turnin.domain.report.application.provider.ReportProviderApi
import com.turnin.domain.report.application.usecase.CreateReportUseCase
import com.turnin.domain.report.application.usecase.GetReportReasonsUseCase
import com.turnin.domain.report.application.usecase.ReportUseCases
import com.turnin.domain.report.domain.repository.ReportRepository
import com.turnin.domain.report.infrastructure.repository.ReportRepositoryImpl
import org.koin.dsl.module

val reportModule = module {
    // Repository
    single<ReportRepository> { ReportRepositoryImpl() }

    // Provider
    single { ReportProviderApi(get()) }

    // Usecase
    single { GetReportReasonsUseCase(get()) }
    single { CreateReportUseCase(get()) }
    single { ReportUseCases(get(), get()) }
}
