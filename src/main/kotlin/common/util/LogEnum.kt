package com.peekr.common.util

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
