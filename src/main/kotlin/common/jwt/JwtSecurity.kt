package com.peekr.common.jwt

import com.peekr.common.jwt.domain.model.value.JwtClaimName
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.jwt.infrastructure.serviceImpl.JwtTokenService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject

fun Application.configureJwtSecurity() {
    val jwtService: JwtTokenService by inject()

    install(Authentication) {
        jwt(jwtService.jwtName) {
            verifier(jwtService.verify())
            realm = jwtService.realm
            validate { credential ->
                if (credential.payload
                        .getClaim(JwtClaimName.Name.name)
                        .asString()
                        .isNotEmpty() &&
                    credential.payload.audience.contains(jwtService.audience)
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                throw TokenException.InvalidTokenException()
            }
        }
    }
}
