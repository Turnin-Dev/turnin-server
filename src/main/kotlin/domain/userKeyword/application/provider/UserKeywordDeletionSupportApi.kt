package com.turnin.domain.userKeyword.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository

/**
 * 외부에 제공할 UserKeyword 삭제 제공 API
 */
class UserKeywordDeletionSupportApi(private val userKeywordRepository: UserKeywordRepository) {
    /**
     * 사용자의 모든 사용자 키워드를 비활성화한다.
     *
     * ###### 해당 메서드는 [userId]의 모든 데이터를 지우므로 주의해서 사용해야 한다.
     *
     * @param userId 비활성화할 사용자 ID
     */
    suspend fun deactivateAll(userId: UserId) =
        userKeywordRepository.deactivateAll(userId)
}
