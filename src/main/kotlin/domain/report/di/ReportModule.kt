package com.peekr.domain.report.di

import com.peekr.domain.report.application.usecase.CreateReportUseCase
import com.peekr.domain.report.application.usecase.GetReportReasonsUseCase
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.infrastructure.repository.ReportRepositoryImpl
import org.koin.dsl.module

val reportModule = module {
    single<ReportRepository> { ReportRepositoryImpl() }

    // Usecase
    factory { GetReportReasonsUseCase(get()) }
    factory { CreateReportUseCase(get()) }
    single<ReportUseCases> { ReportUseCases(get(), get()) }
}
