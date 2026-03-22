package com.peekr.common.firebase

/** FCM data 필드 키 상수 */
object FcmDataKey {
    /** 알림 유형 */
    const val NOTI_TYPE = "noti_type"

    /** 참조 리소스 타입 (ex. USER, POST 등) */
    const val REF_TYPE = "ref_type"

    /** 참조 리소스 ID */
    const val REF_ID = "ref_id"

    /** 알림 제목 */
    const val TITLE = "title"

    /** 알림 본문 */
    const val BODY = "body"

    /** 게시자 사용자 ID (NEW_KEYWORD 알림에서 사용) */
    const val USER_ID = "user_id"
}
