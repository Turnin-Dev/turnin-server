package com.peekr.domain.user.di

import com.peekr.domain.user.application.usecase.GetUserProfileUseCase
import com.peekr.domain.user.application.usecase.GetUserUseCase
import com.peekr.domain.user.application.usecase.UpdateUserUseCase
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.domain.service.UserService
import com.peekr.domain.user.infrastructure.repository.impl.UserRepositoryImpl
import com.peekr.domain.user.infrastructure.service.impl.UserServiceImpl
import org.koin.dsl.module

val userModule = module {
    // Repository
    single<UserRepository> { UserRepositoryImpl() }

    // Service
    single<UserService> { UserServiceImpl(get()) }

    // UseCase
    factory { GetUserUseCase(get()) }
    factory { GetUserProfileUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    single<UserUseCases> { UserUseCases(get(), get(), get()) }
}
