package com.turnin.domain.friend.domain.model

import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId

/**
 * 사용자 정보 일부
 */
data class UserInfo(
    val userId: UserId,
    val displayId: DisplayId,
    val userName: UserName,
    val profileImageUrl: String?,
)
