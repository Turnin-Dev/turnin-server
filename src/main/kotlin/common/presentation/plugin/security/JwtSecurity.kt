package com.peekr.common.presentation.plugin.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

// TODO: JWT 설정 값 하드코딩 말고 한 곳으로 모아두기
fun Application.jwtSecurity() {
    val env = dotenv()
    val jwtAuthName = env["JWT_AUTH_NAME"] ?: "null"
    val realmValue = env["JWT_REALM"] ?: "null"
    val secretKey = env["JWT_SECRET_KEY"] ?: "null"
    val audience = env["JWT_AUDIENCE"] ?: "null"
    val issuer = env["JWT_ISSUER"] ?: "null"
    val algorithm = Algorithm.HMAC256(secretKey)

    install(Authentication) {
        jwt(jwtAuthName) {
            realm = realmValue
            verifier(
                JWT
                    .require(algorithm)
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build(),
            )
            validate { credential ->
                if (credential.payload
                        .getClaim("name")
                        .asString()
                        .isNotEmpty()
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                // TODO: 공통 형식으로 바꾸기
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
