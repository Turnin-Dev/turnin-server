package com.peekr.common.jwt

import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.validator.PeekrValidator.validation
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext

object JWTValidator {
    fun validate(token: String) {
        val parts = token.split(" ")
        validation(parts.first() == "Bearer") {
            "$COMMON_TOKEN_ERROR (Ex. Bearer로 시작해야 함)"
        }
        validation(parts.size == 2) {
            "$COMMON_TOKEN_ERROR (Ex. Bearer [토큰내용])"
        }
    }

    /**
     * JWT 토큰 내 사용자 ID가 올바른 형식으로 포함되어있는지 확인하고 반환한다.
     *
     * (필요시 사용자 ID 추가 검증)
     *
     * @return 문자열 타입의 사용자 ID
     *
     * @throws TokenException.InvalidTokenException 토큰에서 사용자 ID를 찾지못하거나 올바른 형식이 아닌 경우 예외 발생
     */
    fun RoutingContext.getTokenUserId(): String {
        val principal = call.principal<JWTPrincipal>() ?: throw TokenException.InvalidTokenException()
        val authUserIdParam = principal.payload.subject ?: throw TokenException.InvalidTokenException()
        return authUserIdParam
    }

    private const val COMMON_TOKEN_ERROR = "JWT 토큰 형식이 올바르지 않습니다."
}
