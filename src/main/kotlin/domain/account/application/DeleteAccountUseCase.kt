package com.peekr.domain.account.application

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId

/**
 * 계정 삭제
 *
 * @see invoke
 */
class DeleteAccountUseCase {
    /**
     * 계정을 삭제한다.
     *
     * 자세한 내용은 기능 명세서 **`RQ-3`** 참고
     *
     * @param userId 사용자 ID
     */
    suspend operator fun invoke(userId: Long) = suspendTransaction {
        val userIDVO = UserId(userId)

        // 1. 개인 정보 삭제/비식별화 (사용자의 모든 데이터 삭제/비식별화)
        // - 사용자, 사용자 키워드 비활성화
        // - 사용자 ProviderID 변조 ('DELETED_(타임스탬프)_' 접두어 추가)
        // - 이 외 데이터 전부 Hard Delete
        // - 피드/탐색 조회 쿼리 때문에 삭제 시 트랜잭션 내부에서 정확한 순서대로 삭제해야 한다. (사용자, 사용자 키워드 순)

        // 2. 로그아웃 과정 수행 (리프레쉬, FCM 토큰 모두 삭제 포함)

        // 3. 파일 서버 정리 (스토리지 서버에 있는 사용자의 데이터를 모두 삭제)
    }
}
