package com.peekr.common.plugin

import com.peekr.domain.auth.presentation.route.authRoutes
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        customRoutingOption()
        // Add Peekr routes
        authRoutes()
    }
}

private fun Route.customRoutingOption() {
    route("api.json") {
        openApi()
    }
    route("swagger") {
        swaggerUI("/api.json")
    }
}
