package com.turnin.domain.discover.di

import com.turnin.domain.discover.application.usecase.DiscoverUseCases
import com.turnin.domain.discover.application.usecase.GetDiscoverContextUseCase
import com.turnin.domain.discover.domain.repository.DiscoverRepository
import com.turnin.domain.discover.infrastructure.repository.DiscoverRepositoryImpl
import org.koin.dsl.module

val discoverModule = module {
    // Repository
    single<DiscoverRepository> { DiscoverRepositoryImpl() }

    // Usecase
    single { GetDiscoverContextUseCase(get()) }
    single { DiscoverUseCases(get()) }
}
