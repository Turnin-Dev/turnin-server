package com.turnin.common.plugin

import com.turnin.common.util.log.clientIp
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds

/**
 * RateLimit 이름
 *
 * [ktorName] 값으로 사용한다.
 */
enum class RateLimitType(private val value: String) {
    AUTHENTICATED_DEFAULT("authenticated_default"),
    LOGIN("login"),
    REGISTER("register"),
    CREATE_KEYWORD("create_keyword"),
    ADMIN("admin"),
    ;

    val ktorName: RateLimitName
        get() = RateLimitName(this.value)
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
        global {
            rateLimiter(limit = 300, refillPeriod = 60.seconds)
            requestKey { call -> call.getRequestKey() } // 인증 전엔 사실상 IP가 주 키가 됨
        }

        register(RateLimitType.AUTHENTICATED_DEFAULT.ktorName) {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitType.LOGIN.ktorName) {
            rateLimiter(limit = 5, refillPeriod = 10.seconds)
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitType.REGISTER.ktorName) {
            rateLimiter(limit = 2, refillPeriod = 10.seconds)
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitType.CREATE_KEYWORD.ktorName) {
            rateLimiter(limit = 5, refillPeriod = 10.seconds)
            requestKey { call -> call.getRequestKey() }
        }

        register(RateLimitType.ADMIN.ktorName) {
            rateLimiter(limit = 60, refillPeriod = 60.seconds)
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
