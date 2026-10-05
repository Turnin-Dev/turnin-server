package com.turnin.common.firebase

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

    /**
     * 참조 리소스([REF_ID])의 소유자 사용자 ID (예: 키워드 게시물 작성자 ID)
     *
     * 딥링크 화면 이동에 refId 외에 소유자 ID가 필요할 때 사용한다.
     */
    const val REF_OWNER_ID = "ref_owner_id"

    /**
     * 게시자 사용자 ID (NEW_KEYWORD 알림 하위 호환용)
     *
     * [REF_OWNER_ID]로 대체되었다. 구버전 앱이 이 키로 딥링크를 처리하므로,
     * 최소 지원 앱 버전이 [REF_OWNER_ID]를 읽는 버전 이상으로 올라가면 제거한다.
     */
    @Deprecated("REF_OWNER_ID로 대체. 구버전 앱 하위 호환용으로만 사용한다.", ReplaceWith("REF_OWNER_ID"))
    const val USER_ID = "user_id"
}

/**
 * 딥링크용 참조 리소스 타입
 */
object RefType {
    /** 사용자 프로필 */
    const val USER = "USER"

    /** 키워드 게시물 */
    const val KEYWORD = "KEYWORD"
}
