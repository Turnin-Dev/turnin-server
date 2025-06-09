package com.peekr.common.plugin

import com.peekr.common.api.ApiPath
import com.peekr.domain.auth.presentation.route.authRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route(ApiPath.V1.BASE) {
            authRoutes(ApiPath.V1.Auth.ROOT)
        }
    }
}
