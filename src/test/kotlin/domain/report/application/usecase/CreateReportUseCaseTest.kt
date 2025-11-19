package com.peekr.domain.report.application.usecase

import com.peekr.common.db.schema.ReportReasonId
import com.peekr.common.model.UserId
import com.peekr.domain.report.domain.repository.ReportRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow

class CreateReportUseCaseTest {
    private val reportRepository = mockk<ReportRepository>()
    private val usecase = CreateReportUseCase(reportRepository)

    @Test
    fun `신고 생성 성공 테스트`() = runTest {
        // given
        coEvery {
            reportRepository.createReport(any())
        } just Runs

        // when, then
        assertDoesNotThrow {
            usecase(TestReporterId, TestReportedId, TestReportReasonId)
        }
    }

    companion object {
        private val TestReporterId = UserId(1L)
        private val TestReportedId = UserId(2L)
        private val TestReportReasonId = ReportReasonId(1L)
    }
}
