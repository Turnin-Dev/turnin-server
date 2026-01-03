package com.peekr.domain.discover.di

import com.peekr.domain.discover.application.usecase.DiscoverUseCases
import com.peekr.domain.discover.application.usecase.GetDiscoverContextUseCase
import com.peekr.domain.discover.domain.provider.KeywordProvider
import com.peekr.domain.discover.domain.provider.UserProvider
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import com.peekr.domain.discover.infrastructure.provider.KeywordProviderImpl
import com.peekr.domain.discover.infrastructure.provider.UserProviderImpl
import com.peekr.domain.discover.infrastructure.repository.DiscoverRepositoryImpl
import org.koin.dsl.module

val discoverModule = module {
    single<DiscoverRepository> { DiscoverRepositoryImpl() }

    // Provider
    single<UserProvider> { UserProviderImpl(get()) }
    single<KeywordProvider> { KeywordProviderImpl(get()) }

    // Usecase
    factory { GetDiscoverContextUseCase(get(), get(), get()) }
    single<DiscoverUseCases> { DiscoverUseCases(get()) }
}
