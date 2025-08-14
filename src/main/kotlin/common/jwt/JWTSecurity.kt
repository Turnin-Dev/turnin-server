package com.peekr.common.jwt

import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject

// TODO: 추후에 새로운 토큰 발급 후 이전토큰 무효화하는 로직 추가 (1. 버전 업 방식, 2. 블랙리스트 방식)
fun Application.configureJwtSecurity() {
    val jwtService: JWTTokenService by inject()
    val verifier = jwtService.createVerifier(JWTTokenType.Access)

    authentication {
        jwt {
            verifier(verifier)
            realm = jwtService.realm
            validate { credential ->
                val displayIdClaim = credential.payload.getClaim(JWTClaimName.DISPLAY_ID.name)?.asString()
                val hasAudience = credential.payload.audience.contains(jwtService.audience)
                if (displayIdClaim?.isNotEmpty() == true && hasAudience) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { e1, e2 ->
                throw TokenException.InvalidTokenException("cause: $e1, $e2")
            }
        }
    }
}
