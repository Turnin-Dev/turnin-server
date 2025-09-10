package com.peekr.domain.keyword.di

import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCaseImpl
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.domain.repository.UserKeywordRepository
import com.peekr.domain.keyword.domain.service.UserKeywordService
import com.peekr.domain.keyword.infrastructure.repository.impl.KeywordRepositoryImpl
import com.peekr.domain.keyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import com.peekr.domain.keyword.infrastructure.service.impl.UserKeywordServiceImpl
import org.koin.dsl.module

val keywordModule = module {
    // Repository
    single<KeywordRepository> { KeywordRepositoryImpl() }
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // Service
    single<UserKeywordService> { UserKeywordServiceImpl(get(), get()) }

    // UseCase
    single<UserKeywordUseCase> { UserKeywordUseCaseImpl(get()) }
}
