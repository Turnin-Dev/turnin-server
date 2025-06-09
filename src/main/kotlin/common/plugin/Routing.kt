package com.peekr.common.plugin

import com.peekr.domain.auth.presentation.route.authRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        authRoutes()
    }
}
