package com.peekr.domain.userKeyword.di

import com.peekr.domain.userKeyword.application.usecase.CreateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.DeleteUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.GetUserKeywordsUseCase
import com.peekr.domain.userKeyword.application.usecase.UpdateDescriptionUseCase
import com.peekr.domain.userKeyword.application.usecase.UpdateOffsetUseCase
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.infrastructure.provider.KeywordProviderImpl
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import org.koin.dsl.module

val userKeywordModule = module {
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // provider
    single<KeywordProvider> { KeywordProviderImpl(get()) }

    // UseCases
    factory { CreateUserKeywordUseCase(get(), get()) }
    factory { GetUserKeywordsUseCase(get(), get()) }
    factory { UpdateOffsetUseCase(get()) }
    factory { UpdateDescriptionUseCase(get()) }
    factory { DeleteUserKeywordUseCase(get()) }
    single<UserKeywordUseCases> {
        UserKeywordUseCases(get(), get(), get(), get(), get())
    }
}
