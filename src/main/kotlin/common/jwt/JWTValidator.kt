package com.peekr.common.jwt

import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.validator.PeekrValidator.validation
import com.peekr.common.validator.ValidatorException
import com.peekr.domain.core.model.UserId
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
     * 사용자 ID와 JWT 토큰 내에 있는 사용자 ID를 비교하고 유효성 검사를 한다.
     *
     * 유효성 검사에 문제가 없다면 사용자 ID를 반환한다.
     *
     * @param userId 요청으로 들어온 사용자 ID
     *
     * @throws TokenException.UnauthorizedUserException 입력으로 들어온 사용자 ID와 토큰 내 사용자 ID가 일치하지 않는 경우 예외 발생
     * @throws TokenException.InvalidTokenException 토큰에서 사용자 ID를 찾지못하거나 올바른 형식이 아닌 경우 예외 발생
     * @throws ValidatorException 사용자 ID가 올바른 형식이 아닌 경우 예외 발생
     */
    fun RoutingContext.compareAuthUserIdAndMyUserId(userId: UserId) {
        val authUserId = extractUserIdUseToken()
        if (authUserId.value != userId.value) {
            throw TokenException.UnauthorizedUserException()
        }
    }

    /**
     * 인증 토큰에서 사용자 ID를 추출한다.
     *
     * @param [UserId] 사용자 ID
     */
    fun RoutingContext.extractUserIdUseToken(): UserId {
        val principal = call.principal<JWTPrincipal>() ?: throw TokenException.InvalidTokenException()
        val authUserIdParam = principal.payload.subject ?: throw TokenException.InvalidTokenException()
        return try {
            UserId(authUserIdParam.toLong())
        } catch (e: IllegalArgumentException) {
            throw ValidatorException(e.message)
        }
    }

    private const val COMMON_TOKEN_ERROR = "JWT 토큰 형식이 올바르지 않습니다."
}
