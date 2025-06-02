package com.peekr.presentation.plugin

import com.peekr.presentation.route.authRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        authRoutes()
    }
}
