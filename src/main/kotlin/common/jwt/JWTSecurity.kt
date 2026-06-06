package com.turnin.common.jwt

import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.domain.model.JWTTokenType
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.model.Role
import com.turnin.common.plugin.AuthRole
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

// TODO: 보안 강화 로직 추가 예정
//  1. 새로운 토큰 발급 후 이전토큰 무효화하는 로직 추가 (1. 버전 업 방식, 2. 블랙리스트 방식)
//  2. jti DB 대조 로직 추가
fun Application.configureJwtSecurity() {
    val userJwtService: JWTTokenService by inject(named("user"))
    val userVerifier = userJwtService.createVerifier(JWTTokenType.Access)
    val adminJwtService: JWTTokenService by inject(named("admin"))
    val adminVerifier = adminJwtService.createVerifier(JWTTokenType.Access)

    authentication {
        jwt(AuthRole.USER.providerName) {
            verifier(userVerifier)
            realm = userJwtService.realm
            validate { credential ->
                val displayIdClaim = credential.payload.getClaim(JWTClaimName.DISPLAY_ID.name)?.asString()
                val roleClaim = credential.payload.getClaim(JWTClaimName.ROLE.name)?.asString()
                val jwtId = credential.payload.id
                if (
                    displayIdClaim?.isNotEmpty() == true &&
                    roleClaim?.isNotEmpty() == true &&
                    jwtId?.isNotEmpty() == true
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

        jwt(AuthRole.ADMIN.providerName) {
            verifier(adminVerifier)
            realm = adminJwtService.realm
            validate { credential ->
                val displayIdClaim = credential.payload.getClaim(JWTClaimName.DISPLAY_ID.name)?.asString()
                val roleClaim = credential.payload.getClaim(JWTClaimName.ROLE.name)?.asString()
                val jwtId = credential.payload.id
                if (
                    displayIdClaim?.isNotEmpty() == true &&
                    roleClaim == Role.ADMIN.name &&
                    jwtId?.isNotEmpty() == true
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                throw TokenException.UnauthorizedUserException()
            }
        }
    }
}
