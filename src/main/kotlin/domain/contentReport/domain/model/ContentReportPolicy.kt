package com.turnin.domain.contentReport.domain.model

/**
 * 콘텐츠 신고 운영 정책
 */
object ContentReportPolicy {
    /** 콘텐츠를 숨김 처리하는 신고 누적 횟수 */
    const val HIDE_THRESHOLD = 5

    /**
     * 신고 누적 횟수가 숨김 기준 이상인지 확인한다.
     *
     * 기준과 같을 때만이 아닌 이상일 때 숨김 처리하여, 동시 신고로 기준 횟수에서 숨김이 누락되어도 다음 신고 시 숨김 처리되도록 한다.
     *
     * @param reportCount 콘텐츠의 신고 누적 횟수
     */
    fun shouldHide(reportCount: Long): Boolean = reportCount >= HIDE_THRESHOLD
}
