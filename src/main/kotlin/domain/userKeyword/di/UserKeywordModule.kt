package com.peekr.domain.userKeyword.di

import com.peekr.domain.userKeyword.application.provider.KeywordProvider
import com.peekr.domain.userKeyword.application.usecase.CreateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.DeleteUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.GetUserKeywordsUseCase
import com.peekr.domain.userKeyword.application.usecase.UpdateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import org.koin.dsl.module

val userKeywordModule = module {
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // provider
    factory { KeywordProvider(get()) }

    // UseCases
    factory { CreateUserKeywordUseCase(get(), get()) }
    factory { GetUserKeywordsUseCase(get(), get()) }
    factory { UpdateUserKeywordUseCase(get()) }
    factory { DeleteUserKeywordUseCase(get()) }
    single<UserKeywordUseCases> { UserKeywordUseCases(get(), get(), get(), get()) }
}
