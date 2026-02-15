package com.peekr.domain.block.application.usecase

import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.repository.BlockRepository

/**
 * 차단 해제
 *
 * @see invoke
 */
class DeleteBlockUseCase(private val blockRepository: BlockRepository) {
    /**
     * 차단을 해제한다.
     *
     * 반환 값은 [Boolean]타입이지만 의미 없다.
     *
     * @param blockId 차단 ID
     */
    suspend operator fun invoke(
        ownerId: Long,
        blockId: Long,
    ): Boolean {
        val ownerIdVO = UserId(ownerId)
        val blockIdVO = BlockId(blockId)
        return blockRepository.deleteBlock(ownerIdVO, blockIdVO)
    }
}
