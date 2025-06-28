package com.peekr.domain.auth.di

import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.application.usecase.AuthUseCaseImpl
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.domain.service.RefreshTokenService
import com.peekr.domain.auth.infrastructure.repositoryImpl.AuthRepositoryImpl
import com.peekr.domain.auth.infrastructure.repositoryImpl.RefreshTokenRepositoryImpl
import com.peekr.domain.auth.infrastructure.serviceImpl.AuthServiceImpl
import com.peekr.domain.auth.infrastructure.serviceImpl.RefreshTokenServiceImpl
import org.koin.dsl.module

val authModule = module {
    // Domain Service
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            refreshTokenRepository = get(),
            jwtTokenService = get(),
            jwtConfigFactory = get(),
        )
    }
    single<RefreshTokenService> {
        RefreshTokenServiceImpl(get())
    }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl() }

    // UseCase
    single<AuthUseCase> { AuthUseCaseImpl(get(), get()) }
}
