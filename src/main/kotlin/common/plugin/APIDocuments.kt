package com.turnin.common.plugin

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimitRouteSelector

fun Application.configureAPIDocuments() {
    // Install and configure the "OpenApi" Plugin
    install(OpenApi) {
        outputFormat = OutputFormat.JSON

        // rateLimit {} selector가 OpenAPI 경로에 끼어드는 문제 방지
        ignoredRouteSelectors += RateLimitRouteSelector::class

        // 일반 API
        // configure basic information about the api
        info {
            title = "Turnin API"
            description = """
                Turnin API with Swagger-UI

                - 모든 요청의 Global Rate Limit: ${RateLimitToken.GLOBAL.toPrettyString()}
                - Auth, File 라우터를 제외한 인증된 라우터의 Rate Limit: ${RateLimitToken.AUTHENTICATED_DEFAULT.toPrettyString()}
            """.trimIndent()
        }
        // configure the servers from where the api is being served
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        server {
            url = "not yet"
            description = "Production Server"
        }

        // 관리자 API
        spec("admin") {
            info {
                title = "Turnin Admin API"
                description = """
                    Turnin Admin API with Swagger-UI

                    - 모든 관리자 API는 요청 시 필수 헤더를 포함해야 한다. (CF-Access-Client-Id, CF-Access-Client-Secret)
                    - 모든 관리자 요청의 Rate Limit: ${RateLimitToken.ADMIN.toPrettyString()}
                """.trimIndent()
            }
        }
    }
}
