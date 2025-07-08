package com.peekr.common.jwt.di

import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.infrastructure.JWTTokenServiceImpl
import org.koin.dsl.module

val jwtModule = module {
    single<JWTTokenService> { JWTTokenServiceImpl(get()) }
}
