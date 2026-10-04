package com.turnin.domain.contentReport.domain.repository

import com.turnin.common.db.DatabaseException.DuplicatedDataException
import com.turnin.common.db.DatabaseException.ForeignKeyViolationException
import com.turnin.common.model.ContentReportType
import com.turnin.domain.contentReport.domain.model.ContentReportDetail

interface ContentReportRepository {
    /**
     * 콘텐츠 단위로 신고 처리를 직렬화하기 위해 트랜잭션 범위의 잠금을 획득한다.
     *
     * 같은 콘텐츠의 잠금을 다른 트랜잭션이 보유 중이면, 그 트랜잭션이 끝날(커밋/롤백) 때까지 대기한다.
     * 잠금은 트랜잭션이 끝나면 자동으로 해제되므로, 직렬화할 작업과 같은 트랜잭션 안에서 호출해야 한다.
     *
     * @param contentType 콘텐츠 유형
     * @param contentId 콘텐츠 ID
     */
    suspend fun lockContent(
        contentType: ContentReportType,
        contentId: Long,
    )

    /**
     * 콘텐츠 신고를 생성한다.
     *
     * @param contentReportDetail 콘텐츠 신고 디테일
     *
     * @exception DuplicatedDataException 동일 신고자가 동일 콘텐츠를 이미 신고한 경우
     * @exception ForeignKeyViolationException 존재하지 않는 신고 사유인 경우
     */
    suspend fun create(contentReportDetail: ContentReportDetail)

    /**
     * 콘텐츠의 신고 누적 횟수를 조회한다.
     *
     * @param contentType 콘텐츠 유형
     * @param contentId 콘텐츠 ID
     */
    suspend fun countByContent(
        contentType: ContentReportType,
        contentId: Long,
    ): Long
}
