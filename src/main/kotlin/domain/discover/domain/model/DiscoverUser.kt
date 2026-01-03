package com.peekr.domain.discover.domain.model

import com.peekr.common.model.UserName
import com.peekr.common.model.id.UserId

/**
 * 탐색용 사용자 모델
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
data class DiscoverUser(
    val userId: UserId,
    val userName: UserName,
    val profileImageUrl: String?,
)
