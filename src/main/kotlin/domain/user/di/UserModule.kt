package com.peekr.domain.user.di

import com.peekr.domain.user.application.usecase.UserUseCase
import com.peekr.domain.user.application.usecase.UserUseCaseImpl
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.domain.service.UserService
import com.peekr.domain.user.infrastructure.repositoryImpl.UserRepositoryImpl
import com.peekr.domain.user.infrastructure.serviceImpl.UserServiceImpl
import org.koin.dsl.module

val userModule = module {
    single<UserRepository> { UserRepositoryImpl() }

    single<UserService> { UserServiceImpl(get()) }

    single<UserUseCase> { UserUseCaseImpl(get()) }
}
