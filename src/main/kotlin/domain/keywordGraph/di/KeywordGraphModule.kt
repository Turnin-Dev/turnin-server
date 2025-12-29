package com.peekr.domain.keywordGraph.di

import com.peekr.domain.keywordGraph.application.usecase.GetNodeContextUseCase
import com.peekr.domain.keywordGraph.application.usecase.KeywordGraphUseCases
import com.peekr.domain.keywordGraph.domain.provider.KeywordProvider
import com.peekr.domain.keywordGraph.domain.provider.UserProvider
import com.peekr.domain.keywordGraph.domain.repository.KeywordGraphRepository
import com.peekr.domain.keywordGraph.infrastructure.provider.KeywordProviderImpl
import com.peekr.domain.keywordGraph.infrastructure.provider.UserProviderImpl
import com.peekr.domain.keywordGraph.infrastructure.repository.KeywordGraphRepositoryImpl
import org.koin.dsl.module

val keywordGraphModule = module {
    single<KeywordGraphRepository> { KeywordGraphRepositoryImpl() }

    // Provider
    single<UserProvider> { UserProviderImpl(get()) }
    single<KeywordProvider> { KeywordProviderImpl(get()) }

    // Usecase
    factory { GetNodeContextUseCase(get(), get(), get()) }
    single<KeywordGraphUseCases> { KeywordGraphUseCases(get()) }
}
