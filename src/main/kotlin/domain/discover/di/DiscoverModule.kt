package com.peekr.domain.discover.di

import com.peekr.domain.discover.application.usecase.DiscoverUseCases
import com.peekr.domain.discover.application.usecase.GetDiscoverContextUseCase
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.domain.discover.infrastructure.repository.DiscoverRepositoryImpl
import org.koin.dsl.module

val discoverModule = module {
    single<DiscoverRepository> { DiscoverRepositoryImpl() }

    // Usecase
    factory { GetDiscoverContextUseCase(get()) }
    single<DiscoverUseCases> { DiscoverUseCases(get()) }
}
