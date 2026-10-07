package com.turnin.domain.userKeyword.infrastructure.provider

import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.application.provider.PingPongDeletionSupportApi
import com.turnin.domain.userKeyword.domain.provider.PingPongProvider

class PingPongProviderImpl(private val pingPongDeletionSupportApi: PingPongDeletionSupportApi) : PingPongProvider {
    override suspend fun deleteAllByUserKeywordId(userKeywordId: UserKeywordId) =
        pingPongDeletionSupportApi.deleteAllByUserKeywordId(userKeywordId)
}
