package com.turnin.domain.block.application.dto

import com.turnin.domain.block.domain.model.BlockReason

/**
 * 차단 사유 DTO
 *
 * @property id 차단 사유 ID
 * @property code 차단 사유 코드
 * @property description 차단 사유 설명
 */
data class BlockReasonDto(
    val id: Long,
    val code: String,
    val description: String,
)

fun BlockReason.toDto(): BlockReasonDto =
    BlockReasonDto(
        id = id.value,
        code = code,
        description = description,
    )
