package com.peekr.domain.auth.di

import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.application.usecase.AuthUseCaseImpl
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.domain.service.AuthService
import com.peekr.domain.auth.infrastructure.repositoryImpl.AuthRepositoryImpl
import com.peekr.domain.auth.infrastructure.serviceImpl.AuthServiceImpl
import org.koin.dsl.module

val authModule = module {
    // Domain Service
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            jwtTokenService = get(),
        )
    }

    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }

    // UseCase
    single<AuthUseCase> { AuthUseCaseImpl(get()) }
}
