package com.peekr.domain.userKeyword.di

import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCaseImpl
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.domain.service.UserKeywordService
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import com.peekr.domain.userKeyword.infrastructure.service.impl.UserKeywordServiceImpl
import org.koin.dsl.module

val userKeywordModule = module {
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // Service
    single<UserKeywordService> { UserKeywordServiceImpl(get(), get()) }

    // UseCase
    single<UserKeywordUseCase> { UserKeywordUseCaseImpl(get()) }
}
