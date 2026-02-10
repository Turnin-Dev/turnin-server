package com.peekr.domain.block.presentation.dto

import com.peekr.domain.block.application.dto.BlocksPagingDataDto
import kotlinx.serialization.Serializable

/**
 * 차단 목록 응답 바디
 *
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 현재 페이지 크기
 * @property totalSize 전체 크기
 * @property hasNext 다음 페이지 존재 여부
 * @property blocks 친구 목록
 */
@Serializable
data class BlocksResponse(
    val pageNumber: Long,
    val pageSize: Int,
    val totalSize: Long,
    val hasNext: Boolean,
    val blocks: List<BlockResponse>,
) {
    companion object {
        val sample = BlocksResponse(
            pageNumber = 1,
            pageSize = 20,
            totalSize = 100,
            hasNext = true,
            blocks = List(2) {
                val id = it + 1L
                BlockResponse(
                    id = id,
                    blockerId = id,
                    blockedId = id + 2L,
                    reasonId = 1L,
                    customReason = "custom_reason",
                )
            },
        )
    }
}

fun BlocksPagingDataDto.toResponse(): BlocksResponse =
    BlocksResponse(
        pageNumber = pagingData.pageNumber,
        pageSize = pagingData.pageSize,
        totalSize = pagingData.totalSize,
        hasNext = pagingData.hasNext,
        blocks = blocks.map { it.toResponse() },
    )
