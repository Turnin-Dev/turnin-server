package com.peekr.domain.report.application.usecase

import com.peekr.common.model.ReportReasonId
import com.peekr.domain.report.domain.model.ReportReason
import com.peekr.domain.report.domain.repository.ReportRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class GetReportReasonsUseCaseTest {
    private val reportRepository = mockk<ReportRepository>()
    private val usecase = GetReportReasonsUseCase(reportRepository)

    @Test
    fun `신고 사유 조회 성공 테스트`() = runTest {
        // given
        val expectedCount = 5
        val expectedReportReasons = List(5) { TestReportReason }
        coEvery {
            reportRepository.getReportReasons()
        } returns expectedReportReasons

        // when
        val result = usecase()

        // then
        assertEquals(expectedCount, result.size)
    }

    companion object {
        private val TestReportReason = ReportReason(
            id = ReportReasonId(1L),
            code = "code",
            description = "description",
        )
    }
}
