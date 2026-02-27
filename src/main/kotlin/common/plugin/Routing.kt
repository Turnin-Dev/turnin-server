package com.peekr.common.plugin

import com.peekr.common.route.Api
import com.peekr.domain.account.application.AccountUseCases
import com.peekr.domain.account.presentation.accountRoutes
import com.peekr.domain.auth.application.usecase.AuthUseCases
import com.peekr.domain.auth.presentation.route.authRoutes
import com.peekr.domain.block.application.usecase.BlockUseCases
import com.peekr.domain.block.presentation.route.blockRoutes
import com.peekr.domain.discover.application.usecase.DiscoverUseCases
import com.peekr.domain.discover.presentation.route.discoverRoutes
import com.peekr.domain.feed.application.usecase.FeedUseCases
import com.peekr.domain.feed.presentation.route.feedRoutes
import com.peekr.domain.file.application.usecase.FileUseCases
import com.peekr.domain.file.presentation.route.fileRoutes
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.route.friendRoutes
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.route.keywordRoutes
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.presentation.route.reportRoutes
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.presentation.route.userRoutes
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.route.externalUserKeywordRoutes
import com.peekr.domain.userKeyword.presentation.route.userKeywordRoutes
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
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

    routing {
        customRoutingOption()

        // Add Peekr routes
        route(Api.ROUTE, { description = "Peekr API" }) {
            route(Api.V1.ROUTE, { description = "Peekr API V1" }) {
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

                    // 도메인과 API 명세서에 표시되는 위치가 다른 라우트
                    externalUserKeywordRoutes(route = Api.V1.User, usecase = userKeywordUseCases)
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
