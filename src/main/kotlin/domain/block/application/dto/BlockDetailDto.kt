package com.turnin.domain.block.application.dto

import com.turnin.domain.block.domain.model.BlockDetail

/**
 * 차단 디테일 DTO
 *
 * @property blockerId 차단 요청 사용자 ID
 * @property blockedId 차단 당한 사용자 ID
 * @property reasonId 차단 사유 ID
 * @property customReason 기타 차단 사유
 */
data class BlockDetailDto(
    val blockerId: Long,
    val blockedId: Long,
    val reasonId: Long,
    val customReason: String?,
)

fun BlockDetail.toDto(): BlockDetailDto =
    BlockDetailDto(
        blockerId = blockerId.value,
        blockedId = blockedId.value,
        reasonId = reasonId.value,
        customReason = customReason,
    )
