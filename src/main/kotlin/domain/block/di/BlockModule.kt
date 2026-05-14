package com.turnin.domain.block.di

import com.turnin.domain.block.application.provider.BlockDeletionSupportApi
import com.turnin.domain.block.application.usecase.BlockUseCases
import com.turnin.domain.block.application.usecase.CreateBlockUseCase
import com.turnin.domain.block.application.usecase.DeleteBlockUseCase
import com.turnin.domain.block.application.usecase.GetBlockReasonsUseCase
import com.turnin.domain.block.application.usecase.GetBlockedUsersUseCase
import com.turnin.domain.block.domain.provider.FriendProvider
import com.turnin.domain.block.domain.repository.BlockRepository
import com.turnin.domain.block.infrastructure.provider.FriendProviderImpl
import com.turnin.domain.block.infrastructure.repository.BlockRepositoryImpl
import org.koin.dsl.module

val blockModule = module {
    // Repository
    single<BlockRepository> { BlockRepositoryImpl() }

    // Provider
    single<FriendProvider> { FriendProviderImpl(get()) }
    single { BlockDeletionSupportApi(get()) }

    // Usecase
    single { GetBlockReasonsUseCase(get()) }
    single { CreateBlockUseCase(get(), get()) }
    single { GetBlockedUsersUseCase(get()) }
    single { DeleteBlockUseCase(get()) }
    single {
        BlockUseCases(
            get(),
            get(),
            get(),
            get(),
        )
    }
}
