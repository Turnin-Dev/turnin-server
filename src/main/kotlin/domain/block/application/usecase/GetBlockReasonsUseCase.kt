package com.turnin.domain.block.application.usecase

import com.turnin.domain.block.application.dto.BlockReasonDto
import com.turnin.domain.block.application.dto.toDto
import com.turnin.domain.block.domain.repository.BlockRepository

/**
 * 차단 사유 목록 조회
 *
 * @see invoke
 */
class GetBlockReasonsUseCase(private val blockRepository: BlockRepository) {
    /**
     * 차단 사유 목록을 조회한다.
     */
    suspend operator fun invoke(): List<BlockReasonDto> =
        blockRepository.getBlockReasons().map { it.toDto() }
}
