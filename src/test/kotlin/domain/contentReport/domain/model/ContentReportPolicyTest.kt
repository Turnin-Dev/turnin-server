package com.turnin.domain.contentReport.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContentReportPolicyTest {
    @Test
    fun `신고 누적 횟수가 4회이면 숨김 대상이 아니다`() {
        // given
        val reportCount = 4L

        // when
        val result = ContentReportPolicy.shouldHide(reportCount)

        // then
        assertFalse(result)
    }

    @Test
    fun `신고 누적 횟수가 5회이면 숨김 대상이다`() {
        // given
        val reportCount = 5L

        // when
        val result = ContentReportPolicy.shouldHide(reportCount)

        // then
        assertTrue(result)
    }
}
