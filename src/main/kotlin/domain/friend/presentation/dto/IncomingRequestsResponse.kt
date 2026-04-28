package com.turnin.domain.friend.presentation.dto

import com.turnin.domain.friend.application.dto.IncomingRequestPagingDataDto
import kotlinx.serialization.Serializable

/**
 * 받은 친구 요청 목록 응답 바디
 *
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 현재 페이지 크기
 * @property totalSize 전체 크기
 * @property hasNext 다음 페이지 존재 여부
 * @property requesters 요청자 목록
 */
@Serializable
data class IncomingRequestsResponse(
    val pageNumber: Long,
    val pageSize: Int,
    val totalSize: Long,
    val hasNext: Boolean,
    val requesters: List<IncomingRequestInfoResponse>,
) {
    companion object {
        val sample = IncomingRequestsResponse(
            pageNumber = 1,
            pageSize = 10,
            totalSize = 100,
            hasNext = true,
            requesters = listOf(IncomingRequestInfoResponse.sample),
        )
    }
}

fun IncomingRequestPagingDataDto.toResponse(): IncomingRequestsResponse =
    IncomingRequestsResponse(
        pageNumber = pagingData.pageNumber,
        pageSize = pagingData.pageSize,
        totalSize = pagingData.totalSize,
        hasNext = pagingData.hasNext,
        requesters = requests.map { it.toResponse() },
    )
