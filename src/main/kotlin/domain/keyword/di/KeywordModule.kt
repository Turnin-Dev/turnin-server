package com.peekr.domain.keyword.di

import com.peekr.domain.keyword.application.provider.KeywordProviderApi
import com.peekr.domain.keyword.application.usecase.CreateKeywordUseCase
import com.peekr.domain.keyword.application.usecase.GetKeywordByNameUseCase
import com.peekr.domain.keyword.application.usecase.GetKeywordUseCase
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.infrastructure.provider.EmbeddingServiceProviderImpl
import com.peekr.domain.keyword.infrastructure.repository.impl.KeywordRepositoryImpl
import org.koin.dsl.module

val keywordModule = module {
    // Repository
    single<KeywordRepository> { KeywordRepositoryImpl() }

    // UseCases
    factory { GetKeywordUseCase(get()) }
    factory { CreateKeywordUseCase(get(), get()) }
    factory { GetKeywordByNameUseCase(get()) }
    single<KeywordUseCases> { KeywordUseCases(get(), get(), get()) }

    // Provider
    single<EmbeddingServiceProvider> { EmbeddingServiceProviderImpl(get()) }

    // Provider API
    factory { KeywordProviderApi(get(), get()) }
}
