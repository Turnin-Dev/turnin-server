package com.peekr.domain.auth.di

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

    // UseCase
    factory { LoginUseCase(get(), get(), get()) }
    factory { RegisterUseCase(get(), get(), get()) }
    factory { RefreshTokenUseCase(get(), get(), get()) }
    factory { ExistsDisplayIdUseCase(get()) }
    factory { FindUserUseCase(get()) }
    single<AuthUseCases> { AuthUseCases(get(), get(), get(), get(), get()) }
}
