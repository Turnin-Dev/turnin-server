package com.peekr.domain.block.application.usecase

data class BlockUseCases(
    /** @see GetBlockReasonsUseCase */
    val getBlockReasons: GetBlockReasonsUseCase,
    /** @see GetBlockedUsersUseCase */
    val getBlockedUsers: GetBlockedUsersUseCase,
    /** @see CreateBlockUseCase */
    val createBlock: CreateBlockUseCase,
    /** @see DeleteBlockUseCase */
    val deleteBlock: DeleteBlockUseCase,
)
