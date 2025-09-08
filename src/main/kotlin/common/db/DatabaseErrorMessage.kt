package com.peekr.common.db

/** 공통적으로 사용하는 DB 에러 메시지 */
object DatabaseErrorMessage {
    /** 클라이언트에게 노출할 에러 코드 */
    const val CLIENT_COMMON_CODE = "DB_ERROR"

    /** 클라이언트에게 노출할 에러 메시지 */
    const val CLIENT_COMMON_MESSAGE = "데이터베이스 오류가 발생했습니다. 잠시후 다시 시도해주세요."
}
