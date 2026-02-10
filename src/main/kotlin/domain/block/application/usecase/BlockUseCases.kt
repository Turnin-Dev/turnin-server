package com.peekr.domain.block.application.usecase

data class BlockUseCases(
    /** @see GetBlockReasonsUseCase */
    val getBlockReasons: GetBlockReasonsUseCase,
    /** @see GetBlocksUseCase */
    val getBlocks: GetBlocksUseCase,
    /** @see CreateBlockUseCase */
    val createBlock: CreateBlockUseCase,
)
