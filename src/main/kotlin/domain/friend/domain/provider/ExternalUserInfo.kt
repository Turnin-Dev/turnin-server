package com.peekr.domain.friend.domain.provider

import com.peekr.common.model.Name
import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공된 User 정보
 *
 * @property id 사용자 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 */
data class ExternalUserInfo(
    val id: UserId,
    val name: Name,
    val profileImageUrl: String?,
)
