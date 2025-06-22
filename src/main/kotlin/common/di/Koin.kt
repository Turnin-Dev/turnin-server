package com.peekr.common.di

import com.peekr.common.jwt.di.jwtModule
import com.peekr.domain.auth.di.authModule
import com.peekr.domain.user.di.userModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/** Koin 설정 */
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        defaultModule()
        modules(
            jwtModule,
            authModule,
            userModule,
        )
    }
}
