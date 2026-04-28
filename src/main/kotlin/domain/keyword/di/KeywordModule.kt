package com.turnin.domain.keyword.di

import com.turnin.domain.keyword.application.provider.KeywordProviderApi
import com.turnin.domain.keyword.application.usecase.CreateKeywordUseCase
import com.turnin.domain.keyword.application.usecase.GetKeywordByNameUseCase
import com.turnin.domain.keyword.application.usecase.GetKeywordUseCase
import com.turnin.domain.keyword.application.usecase.KeywordUseCases
import com.turnin.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import com.turnin.domain.keyword.infrastructure.provider.EmbeddingServiceProviderImpl
import com.turnin.domain.keyword.infrastructure.repository.impl.KeywordRepositoryImpl
import org.koin.dsl.module

val keywordModule = module {
    // Repository
    single<KeywordRepository> { KeywordRepositoryImpl() }

    // UseCases
    single { GetKeywordUseCase(get()) }
    single { CreateKeywordUseCase(get(), get()) }
    single { GetKeywordByNameUseCase(get()) }
    single { KeywordUseCases(get(), get(), get()) }

    // Provider
    single<EmbeddingServiceProvider> { EmbeddingServiceProviderImpl(get()) }

    // Provider API
    single { KeywordProviderApi(get(), get()) }
}
