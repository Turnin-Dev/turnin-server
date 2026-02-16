package com.peekr.domain.block.di

import com.peekr.domain.block.application.provider.BlockProviderApi
import com.peekr.domain.block.application.usecase.BlockUseCases
import com.peekr.domain.block.application.usecase.CreateBlockUseCase
import com.peekr.domain.block.application.usecase.DeleteBlockUseCase
import com.peekr.domain.block.application.usecase.GetBlockReasonsUseCase
import com.peekr.domain.block.application.usecase.GetBlockUsersUseCase
import com.peekr.domain.block.domain.provider.FriendProvider
import com.peekr.domain.block.domain.repository.BlockRepository
import com.peekr.domain.block.infrastructure.provider.FriendProviderImpl
import com.peekr.domain.block.infrastructure.repository.BlockRepositoryImpl
import org.koin.dsl.module

val blockModule = module {
    // Repository
    single<BlockRepository> { BlockRepositoryImpl() }

    // Provider
    single { BlockProviderApi(get()) }
    single<FriendProvider> { FriendProviderImpl(get()) }

    // Usecase
    factory { GetBlockReasonsUseCase(get()) }
    factory { CreateBlockUseCase(get(), get()) }
    factory { GetBlockUsersUseCase(get()) }
    factory { DeleteBlockUseCase(get()) }
    single<BlockUseCases> { BlockUseCases(get(), get(), get(), get()) }
}
