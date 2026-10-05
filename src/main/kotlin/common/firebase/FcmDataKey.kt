package com.turnin.common.firebase

/**
 * FCM data 기본 필드 키 상수
 *
 * 모든 알림 메시지에 공통으로 담기는 필드의 키이다. 딥링크용 부가 데이터 키는 [RefDataKey]에서 관리한다.
 */
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

/**
 * 딥링크용 부가 데이터(refData) 키
 *
 * refType/refId 외에 화면 이동에 필요한 값을 담는 키로, 알림 내역(notification.ref_data)과 FCM data에 같은 키로 담긴다.
 * [FcmDataKey]의 기본 필드(title, body, noti_type 등)를 refData에 담을 수 없도록 별도 타입으로 분리한다.
 *
 * @property key 실제 저장/전송되는 키 문자열
 */
enum class RefDataKey(val key: String) {
    /** 참조 리소스(ref_id)의 소유자 사용자 ID (예: 키워드 게시물 작성자 ID) */
    REF_OWNER_ID("ref_owner_id"),

    /**
     * 게시자 사용자 ID (NEW_KEYWORD 알림 하위 호환용)
     *
     * [REF_OWNER_ID]로 대체되었다. 구버전 앱이 이 키로 딥링크를 처리하므로,
     * 최소 지원 앱 버전이 [REF_OWNER_ID]를 읽는 버전 이상으로 올라가면 제거한다.
     */
    @Deprecated("REF_OWNER_ID로 대체. 구버전 앱 하위 호환용으로만 사용한다.", ReplaceWith("REF_OWNER_ID"))
    USER_ID("user_id"),
}

/** 딥링크용 부가 데이터를 저장/전송용 문자열 키 맵으로 변환한다. */
fun Map<RefDataKey, String>.toStringKeyMap(): Map<String, String> = mapKeys { it.key.key }
