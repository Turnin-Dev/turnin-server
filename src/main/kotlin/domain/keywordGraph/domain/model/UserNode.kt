package com.peekr.domain.keywordGraph.domain.model

import com.peekr.common.model.Name
import com.peekr.common.model.id.UserId

/**
 * 사용자 노드 모델
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
data class UserNode(
    val userId: UserId,
    val userName: Name,
    val profileImageUrl: String?,
)
