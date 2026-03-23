package com.peekr.domain.userKeyword.domain.provider

import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공되는 키워드 API
 */
interface FriendProvider {
    /**
     * 새 키워드 알림 전송에 필요한 친구 FCM 컨텍스트를 조회한다.
     *
     * @param userId 발신자 ID
     * @return [UserKeywordFriendFcmContext]
     */
    suspend fun getFriendFcmContext(userId: UserId): UserKeywordFriendFcmContext
}
