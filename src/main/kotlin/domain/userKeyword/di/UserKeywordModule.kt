package com.peekr.domain.userKeyword.di

import com.peekr.domain.userKeyword.application.usecase.CreateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.DeleteUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.GetUserKeywordsUseCase
import com.peekr.domain.userKeyword.application.usecase.UpdateUserKeywordUseCasse
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.domain.service.UserKeywordService
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import com.peekr.domain.userKeyword.infrastructure.service.impl.UserKeywordServiceImpl
import org.koin.dsl.module

val userKeywordModule = module {
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // Service
    single<UserKeywordService> { UserKeywordServiceImpl(get(), get()) }

    // UseCases
    factory { CreateUserKeywordUseCase(get()) }
    factory { GetUserKeywordsUseCase(get()) }
    factory { UpdateUserKeywordUseCasse(get()) }
    factory { DeleteUserKeywordUseCase(get()) }
    single<UserKeywordUseCases> { UserKeywordUseCases(get(), get(), get(), get()) }
}
