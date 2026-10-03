package com.turnin.domain.pingPong.domain.model

import com.turnin.common.model.UserName
import com.turnin.common.model.id.UserId

/**
 * 질문자 정보
 *
 * @property userId 질문자 ID
 * @property userName 질문자 이름
 * @property profileImageUrl 질문자 프로필 사진 url
 */
data class PingPongQuestioner(
    val userId: UserId,
    val userName: UserName,
    val profileImageUrl: String?,
)
