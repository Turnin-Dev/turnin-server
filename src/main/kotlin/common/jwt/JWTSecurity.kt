package com.peekr.common.jwt

import com.peekr.common.jwt.domain.model.entity.JWTTokenType
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.ktor.ext.inject

fun Application.configureJwtSecurity() {
    val jwtService: JWTTokenService by inject()
    val verifier = jwtService.createVerifier(JWTTokenType.Access)

    authentication {
        jwt {
            verifier(verifier)
            realm = jwtService.realm
            // 사용자 조회할 때 어떤 에러가 발생하는지 확인하기
            validate { credential ->
                val nameClaim = credential.payload.getClaim(JWTClaimName.Name.name)?.asString()
                val hasAudience = credential.payload.audience.contains(jwtService.audience)
                if (nameClaim?.isNotEmpty() == true && hasAudience) {
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
