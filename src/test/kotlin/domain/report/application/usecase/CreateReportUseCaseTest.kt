package com.peekr.domain.report.application.usecase

import com.peekr.common.model.ReportReasonId
import com.peekr.common.model.UserId
import com.peekr.domain.report.application.dto.ReportDetailDto
import com.peekr.domain.report.domain.repository.ReportRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull

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
            usecase(TestReporterId, TestReportDetailDto)
        }
    }

    @Test
    fun `신고자 ID와 요청자 ID가 같지 않으면 예외가 발생한다`() = runTest {
        // given
        coEvery {
            reportRepository.createReport(any())
        } just Runs

        // when
        val exception = runCatching {
            usecase(UserId(100L), TestReportDetailDto)
        }.exceptionOrNull()

        // then
        assertNotNull(exception)
    }

    companion object {
        private val TestReporterId = UserId(1L)
        private val TestReportedId = UserId(2L)
        private val TestReportReasonId = ReportReasonId(1L)
        private val TestReportDetailDto = ReportDetailDto(
            reporterId = TestReporterId.value,
            reportedId = TestReportedId.value,
            reasonId = TestReportReasonId.value,
            customReason = "custom",
        )
    }
}
