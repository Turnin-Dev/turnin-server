package com.peekr.domain.user.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId

/**
 * 사용자 프로필
 *
 * @property displayId 사용자 표시 ID
 * @property userName 사용자 이름
 * @property profileImageUrl 사용자 프로필 이미지 url
 * @property introduce 사용자 소개 글
 * @property isActive 사용자 활성 여부
 * @property lastLoginAt 마지막 로그인 일시
 * @property friendsCount 사용자 친구 수
 */
data class UserProfile(
    val displayId: DisplayId,
    val userName: UserName,
    val profileImageUrl: String?,
    val introduce: Introduce,
    val isActive: Boolean,
    val lastLoginAt: Long,
    val friendsCount: Long,
)
