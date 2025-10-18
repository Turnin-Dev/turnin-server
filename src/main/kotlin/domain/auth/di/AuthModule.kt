package com.peekr.domain.auth.di

import com.peekr.domain.auth.application.usecase.AuthUseCases
import com.peekr.domain.auth.application.usecase.ExistsDisplayIdUseCase
import com.peekr.domain.auth.application.usecase.FindUserUseCase
import com.peekr.domain.auth.application.usecase.LoginUseCase
import com.peekr.domain.auth.application.usecase.RefreshTokenUseCase
import com.peekr.domain.auth.application.usecase.RegisterUseCase
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService
import com.peekr.domain.auth.infrastructure.repository.impl.AuthRepositoryImpl
import com.peekr.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
import com.peekr.domain.auth.infrastructure.service.impl.AuthServiceImpl
import com.peekr.domain.auth.infrastructure.service.impl.RefreshTokenServiceImpl
import org.koin.dsl.module

val authModule = module {
    // Domain Service
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            refreshTokenRepository = get(),
            jwtTokenService = get(),
        )
    }
    single<RefreshTokenService> {
        RefreshTokenServiceImpl(get())
    }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl() }

    // UseCase
    factory { LoginUseCase(get(), get()) }
    factory { RegisterUseCase(get(), get()) }
    factory { RefreshTokenUseCase(get(), get(), get()) }
    factory { ExistsDisplayIdUseCase(get()) }
    factory { FindUserUseCase(get()) }
    single<AuthUseCases> { AuthUseCases(get(), get(), get(), get(), get()) }
}
