package com.turnin.domain.auth.di

import com.turnin.common.model.Role
import com.turnin.domain.auth.application.provider.AuthDeletionSupportApi
import com.turnin.domain.auth.application.provider.AuthProviderApi
import com.turnin.domain.auth.application.usecase.AuthAdminUseCases
import com.turnin.domain.auth.application.usecase.AuthUseCases
import com.turnin.domain.auth.application.usecase.ExistsDisplayIdUseCase
import com.turnin.domain.auth.application.usecase.FindUserUseCase
import com.turnin.domain.auth.application.usecase.LoginUseCase
import com.turnin.domain.auth.application.usecase.RefreshTokenUseCase
import com.turnin.domain.auth.application.usecase.RegisterUseCase
import com.turnin.domain.auth.application.usecase.ValidateAdminSecretKeyUseCase
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import com.turnin.domain.auth.infrastructure.repository.impl.AuthRepositoryImpl
import com.turnin.domain.auth.infrastructure.repository.impl.RefreshTokenRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule = module {
    // Repository
    single<AuthRepository> { AuthRepositoryImpl() }
    single<RefreshTokenRepository> { RefreshTokenRepositoryImpl() }

    // Provider
    single { AuthProviderApi(get()) }
    single { AuthDeletionSupportApi(get()) }

    // UseCase
    single { LoginUseCase(get(), get(), get(named("user"))) }
    single(named("user")) {
        RegisterUseCase(
            get(),
            get(),
            get(named("user")),
            Role.USER,
        )
    }
    single(named("admin")) {
        RegisterUseCase(
            get(),
            get(),
            get(named("admin")),
            Role.ADMIN,
        )
    }
    single { RefreshTokenUseCase(get(), get(), get(named("user"))) }
    single { ExistsDisplayIdUseCase(get()) }
    single { FindUserUseCase(get()) }
    single { ValidateAdminSecretKeyUseCase(get()) }

    single {
        AuthUseCases(
            login = get(),
            register = get(named("user")),
            refresh = get(),
            existsDisplayId = get(),
            findUser = get(),
        )
    }

    single {
        AuthAdminUseCases(
            register = get(named("admin")),
            existsDisplayId = get(),
            validateAdminSecretKey = get(),
        )
    }
}
