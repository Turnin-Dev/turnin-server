package com.peekr.domain.auth.di

import com.peekr.common.domain.service.JwtTokenProvider
import com.peekr.common.infrastructure.serviceImpl.JwtTokenProviderImpl
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.application.usecase.AuthUseCaseImpl
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.infrastructure.repositoryImpl.AuthRepositoryImpl
import com.peekr.domain.auth.infrastructure.serviceImpl.AuthServiceImpl
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
