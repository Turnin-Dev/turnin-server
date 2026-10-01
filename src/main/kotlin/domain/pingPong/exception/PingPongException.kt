package com.turnin.domain.pingPong.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 핑퐁 커스텀 예외
 */
sealed class PingPongException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 게시물이 없거나, 비활성화/차단 관계로 조회할 수 없는 경우 */
    class UserKeywordNotFound :
        PingPongException(
            code = PingPongErrorCode.UserKeywordNotFound,
            status = HttpStatusCode.NotFound,
        )

    /** 본인 게시물에 질문을 등록하려는 경우 */
    class CannotQuestionOwnUserKeyword :
        PingPongException(
            code = PingPongErrorCode.CannotQuestionOwnUserKeyword,
            status = HttpStatusCode.BadRequest,
        )
}
