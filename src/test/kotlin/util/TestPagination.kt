package com.peekr.util

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.test.runTest

/**
 * 페이지네이션 테스트 유틸 함수
 *
 * 오프셋 기반 페이지네이션을 테스트한다.
 *
 * @param totalSize 전체 항목 크기
 * @param pageSize 페이지 크기
 * @param fetcher 페이지네이션 조회를 수행하는 람다 함수
 */
fun <T> testPagination(
    totalSize: Int,
    pageSize: Int,
    fetcher: suspend (offset: Long, limit: Int) -> List<T>,
) = runTest {
    // ------------------------------ Given ------------------------------
    // 예상 페이지 개수
    val expectedPageCount = totalSize / pageSize
    var currentPage = 1L
    var offset: Long
    // 성공적으로 데이터가 조회된 횟수 (페이지 수)
    var actualFetchedCount = 0

    // ------------------------------ When, Then ------------------------------
    for (i in 0 until expectedPageCount) {
        // 1) offset 계산 (1-based page)
        offset = (currentPage - 1) * pageSize

        // 2) fetcher 람다 실행
        val items = fetcher(offset, pageSize)

        // 3) 예외
        if (items.isEmpty()) {
            fail("예상보다 일찍 빈 목록이 반환되었습니다. (조회 횟수: $actualFetchedCount)")
        }

        // 4) 검증
        assertEquals(pageSize, items.size)

        // 5) 다음 페이지
        currentPage++
        actualFetchedCount++
    }

    // ------------------------------ Then: 최종 검증 ------------------------------
    val finalOffset = (currentPage - 1) * pageSize
    val finalItems = fetcher(finalOffset, pageSize)
    assertTrue(finalItems.isEmpty())
    assertEquals(expectedPageCount, actualFetchedCount)
}
