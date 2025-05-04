package com.peekr

import com.peekr.common.config.configureDatabases
import com.peekr.common.util.configureExceptionHandler
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    configureDatabases()
    configureExceptionHandler()

    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
