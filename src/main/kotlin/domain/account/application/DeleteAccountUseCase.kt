package com.peekr.domain.account.application

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.domain.account.exception.AccountException
import com.peekr.domain.auth.application.provider.AuthDeletionSupportApi
import com.peekr.domain.block.application.provider.BlockDeletionSupportApi
import com.peekr.domain.file.application.provider.FileDeletionSupportApi
import com.peekr.domain.friend.application.provider.FriendDeletionSupportApi
import com.peekr.domain.user.application.provider.UserDeletionSupportApi
import com.peekr.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi

/**
 * 계정 삭제
 *
 * @see invoke
 */
class DeleteAccountUseCase(
    private val authDeletionSupportApi: AuthDeletionSupportApi,
    private val userDeletionSupportApi: UserDeletionSupportApi,
    private val friendDeletionSupportApi: FriendDeletionSupportApi,
    private val blockDeletionSupportApi: BlockDeletionSupportApi,
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi,
    private val fileDeletionSupportApi: FileDeletionSupportApi,
) {
    /**
     * 계정을 삭제한다.
     *
     * 자세한 내용은 기능 명세서 **`RQ-3`** 참고
     *
     * @param userId 사용자 ID
     */
    suspend operator fun invoke(userId: Long) = suspendTransaction {
        val userIDVO = UserId(userId)
        val user = userDeletionSupportApi.findById(userIDVO)
            ?: throw AccountException.UserNotFound()

        // 1. 개인 정보 삭제/비식별화 (사용자의 모든 데이터 삭제/비식별화)
        // - 사용자, 사용자 키워드 비활성화
        // - 사용자 ProviderID 변조 ('DELETED_(타임스탬프)_' 접두어 추가)
        // - 이 외 데이터 전부 Hard Delete
        // - 피드/탐색 조회 쿼리 때문에 삭제 시 트랜잭션 내부에서 정확한 순서대로 삭제해야 한다.

        authDeletionSupportApi.deleteRefreshToken(userIDVO)
        // TODO: 이 부분에 추가로 Notification 삭제 구현 예정
        friendDeletionSupportApi.deleteAll(userIDVO)
        blockDeletionSupportApi.deleteAll(userIDVO)
        userKeywordDeletionSupportApi.deactivateAll(userIDVO)
        userDeletionSupportApi.anonymizeProviderId(userIDVO, user.providerId)
        userDeletionSupportApi.deactivate(userIDVO)

        // 2. 로그아웃 과정을 그대로 수행
        // - 리프레쉬 토큰을 위에서 삭제 완료
        // - FCM 토큰 삭제
        // TODO: FCM 토큰 삭제 구현 예정

        // 3. 파일 서버 정리 (스토리지 서버에 있는 사용자의 데이터를 모두 삭제)
        user.profileImageUrl?.let {
            fileDeletionSupportApi.deleteFile(it)
        }
    }
}
