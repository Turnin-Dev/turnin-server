package com.peekr.domain.user.di

import com.peekr.domain.user.application.provider.UserProviderApi
import com.peekr.domain.user.application.usecase.GetUserProfileUseCase
import com.peekr.domain.user.application.usecase.GetUserUseCase
import com.peekr.domain.user.application.usecase.UpdateIntroduceUseCase
import com.peekr.domain.user.application.usecase.UpdateUserUseCase
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.repository.impl.UserRepositoryImpl
import org.koin.dsl.module

val userModule = module {
    // Provider
    factory { UserProviderApi(get()) }

    // Repository
    single<UserRepository> { UserRepositoryImpl() }

    // UseCase
    factory { GetUserUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    factory { UpdateIntroduceUseCase(get()) }
    single<UserUseCases> { UserUseCases(get(), get(), get(), get()) }
}
