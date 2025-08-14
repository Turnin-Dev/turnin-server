package com.peekr.common.db.schema

/**
 * 친구 관계 상태.
 * - PENDING: 친구 요청이 생성된 상태(승인 대기)
 * - ACCEPTED: 친구 요청이 수락된 상태
 * - REJECTED: 친구 요청이 거절(또는 취소)된 상태
 */
enum class FriendStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}
