package com.turnin.domain.userKeyword.di

import com.turnin.common.di.ApplicationScopeQualifier
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import com.turnin.domain.userKeyword.application.usecase.CreateUserKeywordUseCase
import com.turnin.domain.userKeyword.application.usecase.DeleteUserKeywordUseCase
import com.turnin.domain.userKeyword.application.usecase.GetDetailUseCase
import com.turnin.domain.userKeyword.application.usecase.GetDetailsUseCase
import com.turnin.domain.userKeyword.application.usecase.GetUserKeywordsUseCase
import com.turnin.domain.userKeyword.application.usecase.UpdateUserKeywordUseCase
import com.turnin.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.turnin.domain.userKeyword.domain.provider.FriendProvider
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider
import com.turnin.domain.userKeyword.domain.provider.NotificationProvider
import com.turnin.domain.userKeyword.domain.provider.ReportProvider
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.domain.userKeyword.infrastructure.provider.FriendProviderImpl
import com.turnin.domain.userKeyword.infrastructure.provider.KeywordProviderImpl
import com.turnin.domain.userKeyword.infrastructure.provider.NotificationProviderImpl
import com.turnin.domain.userKeyword.infrastructure.provider.ReportProviderImpl
import com.turnin.domain.userKeyword.infrastructure.repository.impl.UserKeywordRepositoryImpl
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
            get(ApplicationScopeQualifier),
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
