package com.peekr.domain.report.di

import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.infrastructure.repository.ReportRepositoryImpl
import org.koin.dsl.module

val reportModule = module {
    single<ReportRepository> { ReportRepositoryImpl() }
}
