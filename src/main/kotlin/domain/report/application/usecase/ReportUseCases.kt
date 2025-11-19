package com.peekr.domain.report.application.usecase

data class ReportUseCases(
    /** @see GetReportReasonsUseCase */
    val getReportReasons: GetReportReasonsUseCase,
    /** @see CreateReportUseCase */
    val createReport: CreateReportUseCase,
)
