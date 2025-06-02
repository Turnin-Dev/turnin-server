package com.peekr.di

import com.peekr.domain.service.auth.AuthService
import com.peekr.domain.service.auth.JwtTokenProvider
import com.peekr.infrastructure.serviceImpl.AuthServiceImpl
import com.peekr.infrastructure.serviceImpl.JwtTokenProviderImpl
import org.koin.dsl.module

val authModule = module {
    single<JwtTokenProvider> { JwtTokenProviderImpl() }
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            jwtTokenProvider = get(),
        )
    }
}
