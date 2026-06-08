package com.turnin.common.util.log

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    DEBUG,
}

/**
 * 로그 태그이자 MDC의 키가 된다.
 */
enum class LogTag(val key: String) {
    /** 로그 유형 */
    LOG_TYPE("log_type"),

    /** 행위 */
    ACTION("action"),

    /** 사용자 ID */
    USER_ID("user_id"),
}

enum class LogType(val value: String) {
    /** 기본 로그 */
    NORMAL("normal"),

    /** 법적 증적용 개인정보 로그 */
    PRIVACY("privacy"),
}

// 명명 규칙: [도메인]_[행위]_[상태] (일부 생략 가능)
enum class LogAction(val value: String) {
    // 관리자 액션
    ADMIN_AUTH_FAILURE("ADMIN_AUTH_FAILURE"),
    ADMIN_AUTH_SUCCESS("ADMIN_AUTH_SUCCESS"),

    // 회원 액션
    REGISTER_ATTEMPT("REGISTER_ATTEMPT"),
    REGISTER_SUCCESS("REGISTER_SUCCESS"),
    LOGIN_ATTEMPT("LOGIN_ATTEMPT"),
    LOGIN_SUCCESS("LOGIN_SUCCESS"),
    LOGOUT_ATTEMPT("LOGOUT_ATTEMPT"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS"),
    WITHDRAWAL_ATTEMPT("WITHDRAWAL_ATTEMPT"),
    WITHDRAWAL_SUCCESS("WITHDRAWAL_SUCCESS"),

    // 계정/권한 액션
    USER_UPDATE_ATTEMPT("USER_UPDATE_ATTEMPT"),
    USER_UPDATE_SUCCESS("USER_UPDATE_SUCCESS"),
    TOKEN_REFRESH_ATTEMPT("TOKEN_REFRESH_ATTEMPT"),
    TOKEN_REFRESH_SUCCESS("TOKEN_REFRESH_SUCCESS"),
    TOKEN_REFRESH_FAILURE("TOKEN_REFRESH_FAILURE"),
    TOKEN_SAVE_FAILURE("TOKEN_SAVE_FAILURE"),

    // 비즈니스 액션
    REPORT_ATTEMPT("REPORT_ATTEMPT"),
    REPORT_SUCCESS("REPORT_SUCCESS"),
    BLOCK_ATTEMPT("BLOCK_ATTEMPT"),
    BLOCK_SUCCESS("BLOCK_SUCCESS"),

    // 운영/기술적 액션
    FILE_DELETE_FAILURE("FILE_DELETE_FAILURE"),
    FCM_DEACTIVATE_FAILURE("FCM_DEACTIVATE_FAILURE"),
}
