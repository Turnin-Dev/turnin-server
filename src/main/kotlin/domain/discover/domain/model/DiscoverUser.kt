package com.turnin.domain.discover.domain.model

import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId

/**
 * 탐색용 사용자 모델
 *
 * @property id 사용자 ID
 * @property name 사용자 명
 * @property displayId 사용자 표시 ID
 * @property profileImageUrl 사용자 프로필 url
 */
data class DiscoverUser(
    val id: UserId,
    val name: UserName,
    val displayId: DisplayId,
    val profileImageUrl: String?,
)
