package com.turnin.domain.friend.application.dto

/**
 * 나에게 들어온 친구 요청 정보 DTO
 *
 * @property id 친구 ID
 * @property userId 사용자(친구) ID
 * @property displayId 사용자(친구) 표시 ID
 * @property name 사용자(친구) 이름
 * @property profileImageUrl 사용자(친구) 프로필 사진 url
 * @property respondedAt 요청 응답 일자
 * @property createdAt 요청 생성 일자
 * @property updatedAt 요청 수정 일자
 */
data class IncomingRequestInfoDto(
    val id: Long,
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val respondedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
