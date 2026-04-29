package com.turnin.domain.block.domain.model

import com.turnin.common.model.id.BlockReasonId
import com.turnin.common.model.id.UserId
import com.turnin.domain.block.exception.BlockException

/**
 * 차단 디테일 엔티티 모델
 *
 * @property blockerId 차단 요청 사용자 ID
 * @property blockedId 차단 당한 사용자 ID
 * @property reasonId 차단 사유 ID
 * @property customReason 기타 차단 사유
 */
data class BlockDetail(
    val blockerId: UserId,
    val blockedId: UserId,
    val reasonId: BlockReasonId,
    val customReason: String?,
) {
    companion object {
        fun create(
            blockerId: UserId,
            blockedId: UserId,
            reasonId: BlockReasonId,
            customReason: String?,
        ): BlockDetail {
            // 1) 차단 요청 사용자와 차단 당한 사용자가 같으면 안된다.
            if (blockerId == blockedId) {
                throw BlockException.CannotBlockMySelf()
            }

            return BlockDetail(
                blockerId = blockerId,
                blockedId = blockedId,
                reasonId = reasonId,
                customReason = customReason,
            )
        }
    }
}
