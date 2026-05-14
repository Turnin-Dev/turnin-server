package com.turnin.domain.user.di

import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import com.turnin.domain.user.application.provider.UserProviderApi
import com.turnin.domain.user.application.usecase.GetMyProfileUseCase
import com.turnin.domain.user.application.usecase.GetUserProfileUseCase
import com.turnin.domain.user.application.usecase.GetUserUseCase
import com.turnin.domain.user.application.usecase.LogoutUseCase
import com.turnin.domain.user.application.usecase.UpdateIntroduceUseCase
import com.turnin.domain.user.application.usecase.UpdateUserUseCase
import com.turnin.domain.user.application.usecase.UserUseCases
import com.turnin.domain.user.domain.provider.AuthProvider
import com.turnin.domain.user.domain.provider.FileProvider
import com.turnin.domain.user.domain.provider.FriendProvider
import com.turnin.domain.user.domain.provider.NotificationProvider
import com.turnin.domain.user.domain.repository.UserRepository
import com.turnin.domain.user.infrastructure.provider.AuthProviderImpl
import com.turnin.domain.user.infrastructure.provider.FileProviderImpl
import com.turnin.domain.user.infrastructure.provider.FriendProviderImpl
import com.turnin.domain.user.infrastructure.provider.NotificationProviderImpl
import com.turnin.domain.user.infrastructure.repository.impl.UserRepositoryImpl
import org.koin.dsl.module

val userModule = module {
    // Provider
    // 상태 비저장 서비스는 factory 스코프가 아닌 single 스코프로 관리하는 것이 표준 패턴이다.
    // factory 스코프는 매번 주입될 때마다 불필요한 객체 할당을 발생시킨다.
    single { UserProviderApi(get()) }
    single<FriendProvider> { FriendProviderImpl(get()) }
    single<AuthProvider> { AuthProviderImpl(get()) }
    single<FileProvider> { FileProviderImpl(get()) }
    single { UserDeletionSupportApi(get()) }
    single<NotificationProvider> { NotificationProviderImpl(get()) }

    // Repository
    single<UserRepository> { UserRepositoryImpl() }

    // UseCase
    single { GetUserUseCase(get()) }
    single { GetMyProfileUseCase(get(), get()) }
    single { UpdateUserUseCase(get(), get()) }
    single { UpdateIntroduceUseCase(get()) }
    single { GetUserProfileUseCase(get(), get()) }
    single { LogoutUseCase(get(), get()) }
    single {
        UserUseCases(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
