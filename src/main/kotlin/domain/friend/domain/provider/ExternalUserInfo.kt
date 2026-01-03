package com.peekr.domain.friend.domain.provider

import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공된 User 정보
 *
 * @property userId 사용자 ID
 * @property displayId 사용자 표시 ID
 * @property userName 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 */
data class ExternalUserInfo(
    val userId: UserId,
    val displayId: DisplayId,
    val userName: UserName,
    val profileImageUrl: String?,
)
