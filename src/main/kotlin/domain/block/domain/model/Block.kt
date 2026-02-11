package com.peekr.domain.block.domain.model

import com.peekr.common.model.id.BlockId

/**
 * 차단 엔티티 모델
 *
 * @property id 차단 ID
 * @property detail 차단 디테일 엔티티 모델
 */
data class Block(
    val id: BlockId,
    val detail: BlockDetail,
)
