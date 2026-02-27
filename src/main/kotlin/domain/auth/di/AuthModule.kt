package com.peekr.domain.auth.di

import com.peekr.domain.auth.application.provider.AuthDeletionSupportApi
import com.peekr.domain.auth.application.provider.AuthProviderApi
import com.peekr.domain.auth.application.usecase.AuthUseCases
import com.peekr.domain.auth.application.usecase.ExistsDisplayIdUseCase
import com.peekr.domain.auth.application.usecase.FindUserUseCase
import com.peekr.domain.auth.application.usecase.LoginUseCase
import com.peekr.domain.auth.application.usecase.RefreshTokenUseCase
import com.peekr.domain.auth.application.usecase.RegisterUseCase
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.infrastructure.repository.impl.AuthRepositoryImpl
import com.peekr.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
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
