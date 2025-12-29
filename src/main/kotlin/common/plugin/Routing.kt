package com.peekr.common.plugin

import com.peekr.common.route.Api
import com.peekr.domain.auth.application.usecase.AuthUseCases
import com.peekr.domain.auth.presentation.route.authRoutes
import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.presentation.route.fileRoutes
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.route.friendRoutes
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.route.keywordRoutes
import com.peekr.domain.keywordGraph.application.usecase.KeywordGraphUseCases
import com.peekr.domain.keywordGraph.presentation.route.keywordGraphRoutes
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.presentation.route.reportRoutes
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.presentation.route.userRoutes
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.route.userKeywordRoutes
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authUseCases by inject<AuthUseCases>()
    val userUseCases by inject<UserUseCases>()
    val fileUseCase by inject<FileUseCase>()
    val keywordUseCases by inject<KeywordUseCases>()
    val userKeywordUseCases by inject<UserKeywordUseCases>()
    val reportUseCases by inject<ReportUseCases>()
    val friendUseCases by inject<FriendUseCases>()
    val keywordGraphUseCases by inject<KeywordGraphUseCases>()

    routing {
        customRoutingOption()

        // Add Peekr routes
        route(Api.ROUTE, { description = "Peekr API" }) {
            route(Api.V1.ROUTE, { description = "Peekr API V1" }) {
                authRoutes(route = Api.V1.Auth, usecase = authUseCases)
                fileRoutes(route = Api.V1.File, usecase = fileUseCase)
                authenticatedRoute {
                    userRoutes(route = Api.V1.User, usecase = userUseCases)
                    keywordRoutes(route = Api.V1.Keyword, usecase = keywordUseCases)
                    userKeywordRoutes(route = Api.V1.UserKeyword, usecase = userKeywordUseCases)
                    reportRoutes(route = Api.V1.Report, usecase = reportUseCases)
                    friendRoutes(route = Api.V1.Friend, usecase = friendUseCases)
                    keywordGraphRoutes(route = Api.V1.KeywordGraph, usecase = keywordGraphUseCases)
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
