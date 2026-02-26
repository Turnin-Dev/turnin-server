package com.peekr.domain.report.di

import com.peekr.domain.report.application.provider.ReportProviderApi
import com.peekr.domain.report.application.usecase.CreateReportUseCase
import com.peekr.domain.report.application.usecase.GetReportReasonsUseCase
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.infrastructure.repository.ReportRepositoryImpl
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
