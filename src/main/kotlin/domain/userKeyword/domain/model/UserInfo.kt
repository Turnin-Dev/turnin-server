package com.turnin.domain.userKeyword.domain.model

import com.turnin.common.model.UserName
import com.turnin.common.model.id.UserId

/**
 * 사용자 정보 일부
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 프로필 사진 url
 */
data class UserInfo(
    val userId: UserId,
    val userName: UserName,
    val profileImageUrl: String?,
)
