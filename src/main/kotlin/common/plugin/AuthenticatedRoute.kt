package com.turnin.common.plugin

import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.model.id.UserId
import com.turnin.common.validator.ValidatorException
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

fun Route.authenticatedRoute(
    vararg configurations: String? = arrayOf<String?>(null),
    optional: Boolean = false,
    build: AuthenticatedRoute.() -> Unit,
) {
    authenticate(*configurations, optional = optional) {
        AuthenticatedRoute(this).build()
    }
}

class AuthenticatedRoute(private val route: Route) : Route by route {
    /**
     * ##### 해당 함수는 반드시 인증 요청에서만 사용해야 한다.
     *
     * 사용자 ID와 JWT 토큰 내에 있는 사용자 ID를 비교하고 유효성 검사를 한다.
     *
     * 유효성 검사에 문제가 없다면 사용자 ID를 반환한다.
     *
     * @param userIdRequest 요청으로 들어온 사용자 ID
     *
     * @throws TokenException.UnauthorizedUserException 입력으로 들어온 사용자 ID와 토큰 내 사용자 ID가 일치하지 않는 경우 예외 발생
     * @throws TokenException.InvalidTokenException 토큰에서 사용자 ID를 찾지못하거나 올바른 형식이 아닌 경우 예외 발생
     * @throws ValidatorException 사용자 ID가 올바른 형식이 아닌 경우 예외 발생
     */
    fun RoutingContext.verifyAuthUserId(userIdRequest: UserId) {
        val authUserId = extractUserIdWithToken()
        if (authUserId.value != userIdRequest.value) {
            throw TokenException.UnauthorizedUserException()
        }
    }

    /**
     * ##### 해당 함수는 반드시 인증 요청에서만 사용해야 한다.
     *
     * 인증 토큰에서 사용자 ID를 추출한다.
     *
     * @param [UserId] 사용자 ID
     */
    fun RoutingContext.extractUserIdWithToken(): UserId {
        val principal = call.principal<JWTPrincipal>() ?: throw TokenException.InvalidTokenException()
        val authUserIdParam = principal.payload.subject ?: throw TokenException.InvalidTokenException()
        return try {
            UserId(authUserIdParam.toLong())
        } catch (e: IllegalArgumentException) {
            throw ValidatorException(e.message)
        }
    }
}
