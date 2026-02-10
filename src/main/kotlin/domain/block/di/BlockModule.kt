package com.peekr.domain.block.di

import com.peekr.domain.block.domain.repository.BlockRepository
import com.peekr.domain.block.infrastructure.repository.BlockRepositoryImpl
import org.koin.dsl.module

val blockModule = module {
    // Repository
    single<BlockRepository> { BlockRepositoryImpl() }
}
