package com.peekr.domain.user.di

import com.peekr.domain.user.application.provider.UserDeletionSupportApi
import com.peekr.domain.user.application.provider.UserProviderApi
import com.peekr.domain.user.application.usecase.GetMyProfileUseCase
import com.peekr.domain.user.application.usecase.GetUserProfileUseCase
import com.peekr.domain.user.application.usecase.GetUserUseCase
import com.peekr.domain.user.application.usecase.LogoutUseCase
import com.peekr.domain.user.application.usecase.UpdateIntroduceUseCase
import com.peekr.domain.user.application.usecase.UpdateUserUseCase
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.domain.provider.AuthProvider
import com.peekr.domain.user.domain.provider.FileProvider
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.provider.AuthProviderImpl
import com.peekr.domain.user.infrastructure.provider.FileProviderImpl
import com.peekr.domain.user.infrastructure.provider.FriendProviderImpl
import com.peekr.domain.user.infrastructure.repository.impl.UserRepositoryImpl
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

    // Repository
    single<UserRepository> { UserRepositoryImpl() }

    // UseCase
    factory { GetUserUseCase(get()) }
    factory { GetMyProfileUseCase(get(), get()) }
    factory { UpdateUserUseCase(get(), get()) }
    factory { UpdateIntroduceUseCase(get()) }
    factory { GetUserProfileUseCase(get(), get()) }
    factory { LogoutUseCase(get()) }
    single<UserUseCases> {
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
