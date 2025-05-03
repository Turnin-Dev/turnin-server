package com.peekr

import com.peekr.plugin.configureDatabases
import com.peekr.plugin.configureExceptionHandler
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureExceptionHandler()

    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureRouting()
}
