package com.peekr.domain.block.presentation.dto

import com.peekr.domain.block.application.dto.BlockDto
import kotlinx.serialization.Serializable

/**
 * 차단 응답 바디
 *
 * @property id 차단 ID
 * @property blockerId 차단 요청 사용자 ID
 * @property blockedId 차단 당한 사용자 ID
 * @property reasonId 차단 사유 ID
 * @property customReason 기타 차단 사유
 */
@Serializable
data class BlockResponse(
    val id: Long,
    val blockerId: Long,
    val blockedId: Long,
    val reasonId: Long,
    val customReason: String?,
) {
    companion object {
        val sample = BlockResponse(
            id = 1L,
            blockerId = 1L,
            blockedId = 2L,
            reasonId = 1L,
            customReason = "custom_reason",
        )
    }
}

fun BlockDto.toResponse() =
    BlockResponse(
        id = id,
        blockerId = detail.blockerId,
        blockedId = detail.blockedId,
        reasonId = detail.reasonId,
        customReason = detail.customReason,
    )
