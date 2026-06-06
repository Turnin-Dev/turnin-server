package com.turnin.common.jwt.di

import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.infrastructure.JWTTokenServiceImpl
import com.turnin.common.util.config.AppConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jwtModule = module {
    single<JWTTokenService>(named("user")) {
        val appConfig = get<AppConfig>()

        JWTTokenServiceImpl(
            appConfig = appConfig,
            secretKey = appConfig.getOrDefault("ktor.security.jwt.secret", "jwt-secret"),
        )
    }

    single<JWTTokenService>(named("admin")) {
        val appConfig = get<AppConfig>()

        JWTTokenServiceImpl(
            appConfig = appConfig,
            secretKey = appConfig.get("ktor.admin.jwtSecretKey") ?: error("Admin JWT secret key is not configured"),
        )
    }
}
