package com.turnin.domain.block.domain.model

import com.turnin.common.model.UserName
import com.turnin.common.model.id.BlockId
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId

/**
 * 차단 사용자 모델
 *
 * @property id 차단 ID
 * @property userId 차단한 사용자 ID
 * @property displayId 차단한 사용자 표시 ID
 * @property name 차단한 사용자 명
 * @property profileImageUrl 차단한 사용자 프로필 사진 url
 */
data class BlockedUser(
    val id: BlockId,
    val userId: UserId,
    val displayId: DisplayId,
    val name: UserName,
    val profileImageUrl: String?,
)
