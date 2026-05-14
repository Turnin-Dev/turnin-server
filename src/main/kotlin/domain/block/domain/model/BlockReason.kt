package com.turnin.domain.block.domain.model

import com.turnin.common.model.id.BlockReasonId

/**
 * 차단 사유 엔티티 모델
 *
 * @property id 차단 사유 ID
 * @property code 차단 사유 코드
 * @property description 차단 사유 설명
 */
data class BlockReason(
    val id: BlockReasonId,
    val code: String,
    val description: String,
)
