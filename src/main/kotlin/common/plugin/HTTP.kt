package com.peekr.common.plugin

import io.ktor.server.application.Application
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.routing.routing

fun Application.configureHTTP() {
    routing {
        openAPI(path = "openapi")
    }
}
