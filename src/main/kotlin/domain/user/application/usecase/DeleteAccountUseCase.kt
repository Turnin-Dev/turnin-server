package com.peekr.domain.user.application.usecase

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
        // - User 테이블의 is_active 필드를 false로 설정
        // - User 테이블의 name, displayId, providerId 필드를 'DELETED_' 접두어를 붙여 수정
        // - Report 테이블의 reportedUserKeywordId는 null로 설정 (자동 처리)
        // - 이 외 데이터는 전부 삭제

        // 2. 로그아웃 과정 수행 (리프레쉬, FCM 토큰 모두 삭제 포함)

        // 2. 파일 서버 정리 (스토리지 서버에 있는 사용자의 데이터를 모두 삭제)
    }
}
