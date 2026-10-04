package com.turnin.domain.pingPong.exception

import com.turnin.common.exception.ApiErrorCode

/**
 * 핑퐁 커스텀 에러 코드
 */
sealed class PingPongErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    /** 질문을 등록할 게시물(사용자 키워드)을 찾을 수 없는 경우 */
    data object UserKeywordNotFound : PingPongErrorCode(PP001, "게시물을 찾을 수 없습니다.")

    /** 본인 게시물에 질문을 등록하려는 경우 */
    data object CannotQuestionOwnUserKeyword : PingPongErrorCode(PP002, "본인 게시물에는 질문을 등록할 수 없습니다.")

    /** 핑퐁(질문)을 찾을 수 없는 경우 */
    data object PingPongNotFound : PingPongErrorCode(PP003, "핑퐁을 찾을 수 없습니다.")

    /** 게시물 작성자가 아닌 사용자가 답변을 등록하려는 경우 */
    data object NotUserKeywordOwner : PingPongErrorCode(PP004, "게시물 작성자만 답변을 등록할 수 있습니다.")

    /** 이미 답변이 등록된 질문에 답변을 등록하려는 경우 */
    data object AlreadyAnswered : PingPongErrorCode(PP005, "이미 답변이 등록된 질문입니다.")

    /** 차단 관계(양방향)인 질문자의 질문에 답변을 등록하려는 경우 (차단 방향은 노출하지 않는다) */
    data object CannotAnswerBlockedQuestioner : PingPongErrorCode(PP006, "답변할 수 없는 질문입니다.")

    /** 질문자/게시물 작성자가 아닌 사용자가 질문을 삭제하거나, 게시물 작성자가 아닌 사용자가 답변을 삭제하려는 경우 */
    data object NoPermissionToDelete : PingPongErrorCode(PP007, "핑퐁을 삭제할 권한이 없습니다.")

    /** 핑퐁 답변을 찾을 수 없는 경우 */
    data object PingPongAnswerNotFound : PingPongErrorCode(PP008, "답변을 찾을 수 없습니다.")
}

private const val PP001 = "PP001"
private const val PP002 = "PP002"
private const val PP003 = "PP003"
private const val PP004 = "PP004"
private const val PP005 = "PP005"
private const val PP006 = "PP006"
private const val PP007 = "PP007"
private const val PP008 = "PP008"
