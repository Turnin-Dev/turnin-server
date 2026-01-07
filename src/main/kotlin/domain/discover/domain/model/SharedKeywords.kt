package com.peekr.domain.discover.domain.model

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId

/**
 * 유사한 키워드를 공유하고 있는 사용자 정부 일부와 키워드 정보 일부를 담고 있다.
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property userDisplayId 사용자 표시 ID
 * @property userProfileImageUrl 사용자 프로필 사진 URL
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 */
data class SharedUserKeyword(
    val userId: UserId,
    val userName: UserName,
    val userDisplayId: DisplayId,
    val userProfileImageUrl: String?,
    val userKeywordId: UserKeywordId,
    val keywordId: KeywordId,
    val keywordName: KeywordName,
) {
    companion object {
        const val HIGH_SIMILARITY_THRESHOLD = 0.7
        const val MEDIUM_SIMILARITY_THRESHOLD = 0.6
    }
}
