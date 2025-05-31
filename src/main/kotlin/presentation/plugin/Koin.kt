package com.peekr.presentation.plugin

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Koin 설정
 * 모든 모듈은 해당 람다 내부에서 선언되어야 한다.
 *
 * ```
 * /** Example */
 * configureKoin {
 *  modules(appModule)
 * }
 * ```
 *
 * @param module 모듈 블록
 */
fun Application.configureKoin(module: () -> Unit) {
    install(Koin) {
        slf4jLogger()
        module
    }
}
