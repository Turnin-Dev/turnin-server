package com.peekr.domain.block.application.dto

import com.peekr.domain.block.domain.model.Block

/**
 * 차단 DTO
 *
 * @property id 차단 ID
 * @property detail 차단 디테일 엔티티 모델
 */
data class BlockDto(
    val id: Long,
    val detail: BlockDetailDto,
)

fun Block.toDto(): BlockDto =
    BlockDto(
        id = id.value,
        detail = detail.toDto(),
    )
