package com.peekr.domain.discover.domain.provider

import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공되는 사용자 BC API 인터페이스
 */
interface UserProvider {
    /**
     * 사용자 ID 리스트로 사용자 조회
     *
     * @param ids 사용자 ID 리스트
     *
     * @return [ExternalUser]
     */
    suspend fun findByIds(ids: List<UserId>): List<ExternalUser>
}
