package com.turnin.util

import com.turnin.common.exception.configureExceptionHandler
import com.turnin.common.jwt.JWTTestDoubles
import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.model.Role
import com.turnin.common.plugin.AuthRole
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.plugin.authenticatedAdminRoute
import com.turnin.common.plugin.authenticatedUserRoute
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

/**
 * 테스트 유틸로서, 모듈/플러그인/라우팅 등을 설정할 수 있다.
 *
 * @param module Koin 모듈
 * @param plugin 플러그인 (Application 범위)
 * @param routing 라우터 (Routing 범위)
 * @param routingApplicationScope 라우터 (Application 범위)
 */
fun ApplicationTestBuilder.testPlugin(
    role: AuthRole = AuthRole.USER,
    module: Module? = null,
    plugin: Application.() -> Unit = {},
    routing: Routing.() -> Unit = {},
    authRouting: (AuthenticatedRoute.() -> Unit)? = null,
    routingApplicationScope: Application.() -> Unit = {},
) {
    application {
        module?.let {
            if (pluginOrNull(Koin) == null) {
                testKoinModule(module = module)
            }
        }
        testContentNegotiation()
        testExceptionHandler()
        authRouting?.let { testJwtSecurity() }
        plugin()
        routing {
            routing()
            authRouting?.let {
                when (role) {
                    AuthRole.USER -> {
                        authenticatedUserRoute {
                            it()
                        }
                    }

                    AuthRole.ADMIN -> {
                        authenticatedAdminRoute {
                            it()
                        }
                    }
                }
            }
        }
        routingApplicationScope()
    }
}

private fun Application.testJwtSecurity() {
    val testVerifier = JWTTestDoubles.MockVerifier
    val testRealm = JWTTestDoubles.REALM
    val testAudience = JWTTestDoubles.AUDIENCE

    authentication {
        jwt(AuthRole.USER.providerName) {
            verifier(testVerifier)
            realm = testRealm
            validate { credential ->
                val displayIdClaim = credential.payload.getClaim(JWTClaimName.DISPLAY_ID.name)?.asString()
                val hasAudience = credential.payload.audience.contains(testAudience)
                val role = credential.payload.getClaim(JWTClaimName.ROLE.name)?.asString()
                if (
                    displayIdClaim?.isNotEmpty() == true &&
                    role != null &&
                    hasAudience
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { e1, e2 ->
                throw TokenException.InvalidTokenException()
            }
        }

        jwt(AuthRole.ADMIN.providerName) {
            verifier(testVerifier)
            realm = testRealm
            validate { credential ->
                val displayIdClaim = credential.payload.getClaim(JWTClaimName.DISPLAY_ID.name)?.asString()
                val hasAudience = credential.payload.audience.contains(testAudience)
                val role = credential.payload.getClaim(JWTClaimName.ROLE.name)?.asString()
                if (
                    displayIdClaim?.isNotEmpty() == true &&
                    role == Role.ADMIN.name &&
                    hasAudience
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { e1, e2 ->
                throw TokenException.InvalidTokenException()
            }
        }
    }
}

private fun Application.testContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}

private fun Application.testKoinModule(module: Module) {
    install(Koin) {
        allowOverride(true)
        modules(module)
    }
}

private fun Application.testExceptionHandler() {
    configureExceptionHandler()
//    install(StatusPages) {
//        exception<ApiException> { call, cause ->
//            call.respond(
//                status = cause.status,
//                message = ErrorResponse(
//                    code = cause.errorCode.code,
//                    message = cause.message,
//                    status = cause.status.value,
//                ),
//            )
//        }
//
//        exception<Throwable> { call, cause ->
//            call.respond(
//                status = HttpStatusCode.InternalServerError,
//                message = ErrorResponse(
//                    code = INTERNAL_SERVER_ERROR_CODE,
//                    message = cause.localizedMessage ?: UNKNOWN_ERROR_MESSAGE,
//                    status = HttpStatusCode.InternalServerError.value,
//                ),
//            )
//        }
//    }
}
