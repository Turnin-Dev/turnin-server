package com.peekr.common.plugin

import com.peekr.common.api.Api
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.presentation.route.authRoutes
import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.presentation.route.fileRoutes
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.keyword.presentation.route.keywordRoutes
import com.peekr.domain.user.application.usecase.UserUseCase
import com.peekr.domain.user.presentation.route.userRoutes
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authUseCase by inject<AuthUseCase>()
    val userUseCase by inject<UserUseCase>()
    val fileUseCase by inject<FileUseCase>()
    val userKeywordUseCase by inject<UserKeywordUseCase>()

    routing {
        customRoutingOption()

        // Add Peekr routes
        route(Api.ROUTE, { description = "Peekr API" }) {
            route(Api.V1.ROUTE, { description = "Peekr API V1" }) {
                authRoutes(route = Api.V1.Auth, authUseCase = authUseCase)
                fileRoutes(route = Api.V1.File, fileUseCase = fileUseCase)
                authenticate {
                    userRoutes(route = Api.V1.User, userUseCase = userUseCase)
                    keywordRoutes(route = Api.V1.Keyword, userKeywordUseCase = userKeywordUseCase)
                }
            }
        }
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
