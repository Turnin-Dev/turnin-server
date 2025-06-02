package com.peekr.di

import com.peekr.application.usecase.auth.AuthUseCase
import com.peekr.application.usecase.auth.AuthUseCaseImpl
import com.peekr.domain.repository.auth.AuthRepository
import com.peekr.domain.service.auth.AuthService
import com.peekr.domain.service.auth.JwtTokenProvider
import com.peekr.infrastructure.repositoryImpl.AuthRepositoryImpl
import com.peekr.infrastructure.serviceImpl.AuthServiceImpl
import com.peekr.infrastructure.serviceImpl.JwtTokenProviderImpl
import org.koin.dsl.module

val authModule = module {
    // JWT Provider
    single<JwtTokenProvider> { JwtTokenProviderImpl() }

    // Domain Service
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            jwtTokenProvider = get(),
        )
    }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }

    // UseCase
    single<AuthUseCase> { AuthUseCaseImpl(get()) }
}
