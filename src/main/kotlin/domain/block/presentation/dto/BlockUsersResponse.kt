package com.peekr.domain.block.presentation.dto

import com.peekr.domain.block.application.dto.BlockUsersPagingDataDto
import kotlinx.serialization.Serializable

/**
 * 차단 사용자 목록 응답 바디
 *
 * @property pageNumber 현재 페이지 번호
 * @property pageSize 현재 페이지 크기
 * @property hasNext 다음 페이지 존재 여부
 * @property blockUsers 차단 사용자 목록
 */
@Serializable
data class BlockUsersResponse(
    val pageNumber: Long,
    val pageSize: Int,
    val hasNext: Boolean,
    val blockUsers: List<BlockUserResponse>,
) {
    companion object {
        val sample = BlockUsersResponse(
            pageNumber = 1,
            pageSize = 20,
            hasNext = true,
            blockUsers = List(2) {
                val id = it + 1L
                BlockUserResponse(
                    id = id,
                    userId = id,
                    displayId = "did$id",
                    name = "name$it",
                    profileImageUrl = "profileImageUrl$it",
                )
            },
        )
    }
}

fun BlockUsersPagingDataDto.toResponse(): BlockUsersResponse =
    BlockUsersResponse(
        pageNumber = pagingData.pageNumber,
        pageSize = pagingData.pageSize,
        hasNext = pagingData.hasNext,
        blockUsers = blockUsers.map { it.toResponse() },
    )
