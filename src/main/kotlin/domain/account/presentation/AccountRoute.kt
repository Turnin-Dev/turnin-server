package com.peekr.domain.account.presentation

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.domain.account.application.AccountUseCases
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

fun AuthenticatedRoute.accountRoutes(route: Api.V1.Account, usecase: AccountUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Account API"
    }) {
        delete({ deleteAccountDocs() }) {
            val userId = extractUserIdWithToken()
            usecase.delete(userId.value)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun RouteConfig.deleteAccountDocs() {
    summary = "계정 삭제"
    description = "계정을 삭제한다. (일부 데이터는 비활성화 처리)"
    response {
        code(HttpStatusCode.OK) {
            description = "계정 삭제 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자를 찾을 수 없는 경우"
        }
    }
}
