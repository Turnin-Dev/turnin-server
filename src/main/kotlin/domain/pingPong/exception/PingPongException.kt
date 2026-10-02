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

    /** 핑퐁(질문)이 없거나, 신고 누적으로 숨김 처리된 경우 */
    class PingPongNotFound :
        PingPongException(
            code = PingPongErrorCode.PingPongNotFound,
            status = HttpStatusCode.NotFound,
        )

    /** 게시물 작성자가 아닌 사용자가 답변을 등록하려는 경우 */
    class NotUserKeywordOwner :
        PingPongException(
            code = PingPongErrorCode.NotUserKeywordOwner,
            status = HttpStatusCode.Forbidden,
        )

    /** 이미 답변이 등록된 질문에 답변을 등록하려는 경우 */
    class AlreadyAnswered(cause: Throwable? = null) :
        PingPongException(
            code = PingPongErrorCode.AlreadyAnswered,
            status = HttpStatusCode.Conflict,
            cause = cause,
        )
}
