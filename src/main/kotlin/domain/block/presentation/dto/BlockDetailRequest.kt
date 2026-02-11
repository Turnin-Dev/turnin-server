package com.peekr.domain.block.presentation.dto

import com.peekr.domain.block.application.dto.BlockDetailDto
import kotlinx.serialization.Serializable

/**
 * 차단 디테일 DTO
 *
 * @property blockerId 차단 요청 사용자 ID
 * @property blockedId 차단 당한 사용자 ID
 * @property reasonId 차단 사유 ID
 * @property customReason 기타 차단 사유
 */
@Serializable
data class BlockDetailRequest(
    val blockerId: Long,
    val blockedId: Long,
    val reasonId: Long,
    val customReason: String?,
) {
    companion object {
        val sample = BlockDetailRequest(
            blockerId = 1L,
            blockedId = 2L,
            reasonId = 1,
            customReason = "sample",
        )
    }
}

fun BlockDetailRequest.toDto(): BlockDetailDto =
    BlockDetailDto(
        blockerId = blockerId,
        blockedId = blockedId,
        reasonId = reasonId,
        customReason = customReason,
    )
