package com.turnin.common.plugin

import com.turnin.common.util.log.clientIp
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.RateLimitProviderConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
    CREATE_PING_PONG_BURST("create_ping_pong_burst", limit = 1, refillPeriod = 5.seconds),
    CREATE_PING_PONG("create_ping_pong", limit = 5, refillPeriod = 60.seconds),
    ADMIN("admin", limit = 60, refillPeriod = 60.seconds),
    ;

    val ktorName: RateLimitName
        get() = RateLimitName(this.value)

    fun toPrettyString(): String =
        "$limit req / ${refillPeriod.toString(DurationUnit.SECONDS)}"
}

/**
 * RateLimiter 적용 헬퍼 함수
 *
 * [RateLimitToken]을 사용하여 적용한다.
 *
 * @param token [RateLimitToken]
 * @param requestKeySelector RateLimit 측정 기준이 될 요청 키
 */
private fun RateLimitProviderConfig.applyRateLimiter(
    token: RateLimitToken,
    requestKeySelector: (ApplicationCall) -> Any = { it.getRequestKey() },
) {
    rateLimiter(limit = token.limit, refillPeriod = token.refillPeriod)
    requestKey { call -> requestKeySelector(call) }
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
            applyRateLimiter(RateLimitToken.GLOBAL) // 인증 전엔 사실상 IP가 주 키가 됨
        }

        // ------------------------------ 비인증(키 -> ip) ------------------------------
        register(RateLimitToken.LOGIN.ktorName) {
            applyRateLimiter(RateLimitToken.LOGIN)
        }

        register(RateLimitToken.REGISTER.ktorName) {
            applyRateLimiter(RateLimitToken.REGISTER)
        }

        register(RateLimitToken.EXISTS_CHECK.ktorName) {
            applyRateLimiter(RateLimitToken.EXISTS_CHECK)
        }

        register(RateLimitToken.TOKEN_REFRESH.ktorName) {
            applyRateLimiter(
                token = RateLimitToken.TOKEN_REFRESH,
                requestKeySelector = { call ->
                    call.request.headers["Authorization"]
                        ?: call.getRequestKey()
                },
            )
        }

        // ------------------------------ 인증(키 -> 사용자 ID) ------------------------------
        register(RateLimitToken.AUTHENTICATED_DEFAULT.ktorName) {
            applyRateLimiter(RateLimitToken.AUTHENTICATED_DEFAULT)
        }

        register(RateLimitToken.CREATE_KEYWORD.ktorName) {
            applyRateLimiter(RateLimitToken.CREATE_KEYWORD)
        }

        // 핑퐁 작성: 동일 사용자가 동일 게시물에 5초당 1회 / 1분당 5회 (두 토큰을 중첩 적용)
        register(RateLimitToken.CREATE_PING_PONG_BURST.ktorName) {
            applyRateLimiter(
                token = RateLimitToken.CREATE_PING_PONG_BURST,
                requestKeySelector = { call -> call.getUserKeywordScopedRequestKey() },
            )
        }

        register(RateLimitToken.CREATE_PING_PONG.ktorName) {
            applyRateLimiter(
                token = RateLimitToken.CREATE_PING_PONG,
                requestKeySelector = { call -> call.getUserKeywordScopedRequestKey() },
            )
        }

        // ------------------------------ 혼용 ------------------------------
        register(RateLimitToken.ADMIN.ktorName) {
            applyRateLimiter(RateLimitToken.ADMIN)
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

/**
 * 게시물(userKeyword) 단위 RateLimit 요청 키
 *
 * `{userKeywordId}` 경로 파라미터가 있는 라우트에서 사용한다.
 *
 * 경로 파라미터가 없는 라우트에 적용하면 키가 `"{사용자 키}:null"`이 되어 게시물 구분 없이 사용자 단위로 제한된다.
 */
private fun ApplicationCall.getUserKeywordScopedRequestKey(): Any =
    "${getRequestKey()}:${parameters["userKeywordId"]}"
