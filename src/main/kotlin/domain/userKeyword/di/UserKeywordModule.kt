package com.peekr.domain.userKeyword.di

import com.peekr.common.di.BackgroundScopeQualifier
import com.peekr.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import com.peekr.domain.userKeyword.application.usecase.CreateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.DeleteUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.GetDetailUseCase
import com.peekr.domain.userKeyword.application.usecase.GetDetailsUseCase
import com.peekr.domain.userKeyword.application.usecase.GetUserKeywordsUseCase
import com.peekr.domain.userKeyword.application.usecase.UpdateUserKeywordUseCase
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.domain.provider.FriendProvider
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.provider.NotificationProvider
import com.peekr.domain.userKeyword.domain.provider.ReportProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.infrastructure.provider.FriendProviderImpl
import com.peekr.domain.userKeyword.infrastructure.provider.KeywordProviderImpl
import com.peekr.domain.userKeyword.infrastructure.provider.NotificationProviderImpl
import com.peekr.domain.userKeyword.infrastructure.provider.ReportProviderImpl
import com.peekr.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
import org.koin.dsl.module

val userKeywordModule = module {
    // Repository
    single<UserKeywordRepository> { UserKeywordRepositoryImpl() }

    // Provider
    single<KeywordProvider> { KeywordProviderImpl(get()) }
    single<ReportProvider> { ReportProviderImpl(get()) }
    single { UserKeywordDeletionSupportApi(get()) }
    single<FriendProvider> { FriendProviderImpl(get()) }
    single<NotificationProvider> { NotificationProviderImpl(get()) }

    // UseCases
    single {
        CreateUserKeywordUseCase(
            get(),
            get(),
            get(),
            get(),
            get(BackgroundScopeQualifier),
        )
    }
    single { GetUserKeywordsUseCase(get(), get()) }
    single { UpdateUserKeywordUseCase(get(), get()) }
    single { DeleteUserKeywordUseCase(get(), get()) }
    single { GetDetailUseCase(get()) }
    single { GetDetailsUseCase(get()) }
    single {
        UserKeywordUseCases(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
