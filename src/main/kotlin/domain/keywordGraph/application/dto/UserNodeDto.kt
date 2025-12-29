package com.peekr.domain.keywordGraph.application.dto

import com.peekr.common.model.id.UserId

/**
 * 사용자 노드 모델 DTO
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
data class UserNodeDto(
    val userId: UserId,
    val userName: String,
    val profileImageUrl: String?,
)
