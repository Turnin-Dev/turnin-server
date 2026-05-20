package com.turnin.common.plugin

import com.turnin.common.route.Api
import com.turnin.common.util.config.AppConfig
import com.turnin.common.util.config.RunEnvironment
import com.turnin.common.util.config.RunEnvironment.Companion.toRunEnvironment
import com.turnin.common.util.healthRoutes
import com.turnin.domain.account.application.AccountUseCases
import com.turnin.domain.account.presentation.accountRoutes
import com.turnin.domain.auth.application.usecase.AuthUseCases
import com.turnin.domain.auth.presentation.route.authRoutes
import com.turnin.domain.block.application.usecase.BlockUseCases
import com.turnin.domain.block.presentation.route.blockRoutes
import com.turnin.domain.discover.application.usecase.DiscoverUseCases
import com.turnin.domain.discover.presentation.route.discoverRoutes
import com.turnin.domain.feed.application.usecase.FeedUseCases
import com.turnin.domain.feed.presentation.route.feedRoutes
import com.turnin.domain.file.application.usecase.FileUseCases
import com.turnin.domain.file.presentation.route.fileRoutes
import com.turnin.domain.friend.application.usecase.FriendUseCases
import com.turnin.domain.friend.presentation.route.friendRoutes
import com.turnin.domain.keyword.application.usecase.KeywordUseCases
import com.turnin.domain.keyword.presentation.route.keywordRoutes
import com.turnin.domain.notification.application.usecase.NotificationUseCases
import com.turnin.domain.notification.presentation.route.notificationRoutes
import com.turnin.domain.report.application.usecase.ReportUseCases
import com.turnin.domain.report.presentation.route.reportRoutes
import com.turnin.domain.user.application.usecase.UserUseCases
import com.turnin.domain.user.presentation.route.userRoutes
import com.turnin.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.turnin.domain.userKeyword.presentation.route.externalUserKeywordRoutes
import com.turnin.domain.userKeyword.presentation.route.userKeywordRoutes
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import kotlin.getValue
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val appConfig = get<AppConfig>()
    val environment = appConfig.getOrDefault("ktor.environment", "")

    val accountUseCases by inject<AccountUseCases>()
    val authUseCases by inject<AuthUseCases>()
    val userUseCases by inject<UserUseCases>()
    val fileUseCases by inject<FileUseCases>()
    val keywordUseCases by inject<KeywordUseCases>()
    val userKeywordUseCases by inject<UserKeywordUseCases>()
    val reportUseCases by inject<ReportUseCases>()
    val friendUseCases by inject<FriendUseCases>()
    val discoverUseCases by inject<DiscoverUseCases>()
    val feedUseCases by inject<FeedUseCases>()
    val blockUseCases by inject<BlockUseCases>()
    val notificationUseCases by inject<NotificationUseCases>()

    routing {
        customRoutingOption(environment.toRunEnvironment())

        // Add Turnin routes
        route(Api.ROUTE, { description = "Turnin API" }) {
            healthRoutes(route = Api.Health)
            route(Api.V1.ROUTE, { description = "Turnin API V1" }) {
                authRoutes(route = Api.V1.Auth, usecase = authUseCases)
                fileRoutes(route = Api.V1.File, usecase = fileUseCases)
                authenticatedRoute {
                    accountRoutes(route = Api.V1.Account, usecase = accountUseCases)
                    userRoutes(route = Api.V1.User, usecase = userUseCases)
                    keywordRoutes(route = Api.V1.Keyword, usecase = keywordUseCases)
                    userKeywordRoutes(route = Api.V1.UserKeyword, usecase = userKeywordUseCases)
                    reportRoutes(route = Api.V1.Report, usecase = reportUseCases)
                    friendRoutes(route = Api.V1.Friend, usecase = friendUseCases)
                    discoverRoutes(route = Api.V1.Discover, usecase = discoverUseCases)
                    feedRoutes(route = Api.V1.Feed, usecase = feedUseCases)
                    blockRoutes(route = Api.V1.Block, usecase = blockUseCases)
                    notificationRoutes(route = Api.V1.Notification, usecase = notificationUseCases)

                    // 도메인과 API 명세서에 표시되는 위치가 다른 라우트
                    externalUserKeywordRoutes(route = Api.V1.User, usecase = userKeywordUseCases)
                }
            }
        }
    }
}

private fun Route.customRoutingOption(runEnvironment: RunEnvironment) {
    if (runEnvironment != RunEnvironment.Prod) {
        route("api.json") {
            openApi()
        }
        route("swagger") {
            swaggerUI("/api.json")
        }
    }
}
