package com.turnin.domain.auth.di

import com.turnin.domain.auth.application.provider.AuthDeletionSupportApi
import com.turnin.domain.auth.application.provider.AuthProviderApi
import com.turnin.domain.auth.application.usecase.AuthUseCases
import com.turnin.domain.auth.application.usecase.ExistsDisplayIdUseCase
import com.turnin.domain.auth.application.usecase.FindUserUseCase
import com.turnin.domain.auth.application.usecase.LoginUseCase
import com.turnin.domain.auth.application.usecase.RefreshTokenUseCase
import com.turnin.domain.auth.application.usecase.RegisterUseCase
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import com.turnin.domain.auth.infrastructure.repository.impl.AuthRepositoryImpl
import com.turnin.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
import org.koin.dsl.module

val authModule = module {
    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl() }

    // Provider
    single { AuthProviderApi(get()) }
    single { AuthDeletionSupportApi(get()) }

    // UseCase
    single { LoginUseCase(get(), get(), get()) }
    single { RegisterUseCase(get(), get(), get()) }
    single { RefreshTokenUseCase(get(), get(), get()) }
    single { ExistsDisplayIdUseCase(get()) }
    single { FindUserUseCase(get()) }
    single {
        AuthUseCases(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
