package com.turnin.common.jwt.di

import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.infrastructure.JWTTokenServiceImpl
import org.koin.dsl.module

val jwtModule = module {
    single<JWTTokenService> { JWTTokenServiceImpl(get()) }
}
