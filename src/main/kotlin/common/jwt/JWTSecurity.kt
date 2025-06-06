package com.peekr.common.jwt

import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.common.jwt.infrastructure.JWTTokenServiceImpl
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject

fun Application.configureJwtSecurity() {
    val jwtService: JWTTokenServiceImpl by inject()
    val jwtConfigFactory: JWTConfigFactory by inject()
    val verifier = jwtConfigFactory.createVerifier(jwtService.getVerifierConfig())

    authentication {
        jwt {
            verifier(verifier)
            realm = jwtService.realm
            validate { credential ->
                if (credential.payload
                        .getClaim(JWTClaimName.Name.name)
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
