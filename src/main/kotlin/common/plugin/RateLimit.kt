package com.turnin.common.plugin

import com.turnin.common.util.log.clientIp
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * RateLimit 토큰
 *
 * [ktorName] 값으로 사용한다.
 */
enum class RateLimitToken(
    private val value: String,
    val limit: Int,
    val refillPeriod: Duration,
) {
    GLOBAL("global", limit = 1000, refillPeriod = 60.seconds),
    LOGIN("login", limit = 5, refillPeriod = 10.seconds),
    REGISTER("register", limit = 2, refillPeriod = 10.seconds),
    EXISTS_CHECK("exists_check", limit = 30, refillPeriod = 10.seconds),
    TOKEN_REFRESH("token_refresh", limit = 10, refillPeriod = 60.seconds),
    AUTHENTICATED_DEFAULT("authenticated_default", limit = 100, refillPeriod = 60.seconds),
    CREATE_KEYWORD("create_keyword", limit = 5, refillPeriod = 10.seconds),
    ADMIN("admin", limit = 60, refillPeriod = 60.seconds),
    ;

    val ktorName: RateLimitName
        get() = RateLimitName(this.value)

    fun toPrettyString(): String =
        "$limit req / $refillPeriod"
}

/**
 * RateLimit 설정
 *
 * `limit`: 버킷 용량 (몇 개까지 허용할지)
 *
 * `refillPeriod`: 그 용량이 다시 꽉 차는 데 걸리는 시간'
 *
 * `requestKey`: 어떤 기준으로 카운트할지 (기본은 전체 요청이 공유, IP 별로 나누려면 직접 지정 필요)
 */
fun Application.configureRateLimit() {
    install(RateLimit) {
        // ------------------------------ 전역 ------------------------------
        global {
            rateLimiter(
                limit = RateLimitToken.GLOBAL.limit,
                refillPeriod = RateLimitToken.GLOBAL.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() } // 인증 전엔 사실상 IP가 주 키가 됨
        }

        // ------------------------------ 비인증(키 -> ip) ------------------------------
        register(RateLimitToken.LOGIN.ktorName) {
            rateLimiter(
                limit = RateLimitToken.LOGIN.limit,
                refillPeriod = RateLimitToken.LOGIN.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitToken.REGISTER.ktorName) {
            rateLimiter(
                limit = RateLimitToken.REGISTER.limit,
                refillPeriod = RateLimitToken.REGISTER.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitToken.EXISTS_CHECK.ktorName) {
            rateLimiter(
                limit = RateLimitToken.EXISTS_CHECK.limit,
                refillPeriod = RateLimitToken.EXISTS_CHECK.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitToken.TOKEN_REFRESH.ktorName) {
            rateLimiter(
                limit = RateLimitToken.TOKEN_REFRESH.limit,
                refillPeriod = RateLimitToken.TOKEN_REFRESH.refillPeriod,
            )
            requestKey { call ->
                call.request.headers["Authorization"]
                    ?: call.getRequestKey()
            }
        }

        // ------------------------------ 인증(키 -> 사용자 ID) ------------------------------
        register(RateLimitToken.AUTHENTICATED_DEFAULT.ktorName) {
            rateLimiter(
                limit = RateLimitToken.AUTHENTICATED_DEFAULT.limit,
                refillPeriod = RateLimitToken.AUTHENTICATED_DEFAULT.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitToken.CREATE_KEYWORD.ktorName) {
            rateLimiter(
                limit = RateLimitToken.CREATE_KEYWORD.limit,
                refillPeriod = RateLimitToken.CREATE_KEYWORD.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }

        // ------------------------------ 혼용 ------------------------------
        register(RateLimitToken.ADMIN.ktorName) {
            rateLimiter(
                limit = RateLimitToken.ADMIN.limit,
                refillPeriod = RateLimitToken.ADMIN.refillPeriod,
            )
            requestKey { call -> call.getRequestKey() }
        }
    }
}

/**
 * RateLimit 요청 키
 *
 * 요청 키 우선순위
 * - JWT 토큰 사용자 ID
 * - IP
 */
private fun ApplicationCall.getRequestKey(): Any =
    this.principal<JWTPrincipal>()?.payload?.subject ?: this.clientIp()
