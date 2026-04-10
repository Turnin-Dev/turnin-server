package com.peekr.common.util

enum class LogTag(val key: String) {
    /** 로그 유형 */
    LOG_TYPE("log_type"),

    /** 행위 */
    ACTION("action"),

    /** 사용자 ID */
    USER_ID("user_id"),

    /** 클라이언트 IP 주소 */
    CLIENT_IP("client_ip"),

    /** 요청 URL */
    REQUEST_URL("request_url"),

    /** 요청 메서드 */
    REQUEST_METHOD("request_method"),

    /** 예외 유형 */
    EXCEPTION_TYPE("exception_type"),

    /** 상태 코드 */
    STATUS_CODE("status_code"),

    /** 비즈니스 에러 코드 */
    ERROR_CODE("error_code"),
}

enum class LogType(val value: String) {
    /** 기본 로그 */
    NORMAL("normal"),

    /** 법적 증적용 개인정보 로그 */
    PRIVACY("privacy"),
}
