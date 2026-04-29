package com.turnin.domain.user.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.user.application.dto.UserDto
import com.turnin.domain.user.application.dto.toDto
import com.turnin.domain.user.domain.repository.UserRepository

/**
 * 외부에 제공할 User 삭제 제공 API
 */
class UserDeletionSupportApi(private val userRepository: UserRepository) {
    /**
     * ID를 통해 사용자를 조회한다.
     *
     * @param id 조회할 사용자 ID
     *
     * @return [UserDto] - 사용자를 찾을 수 없으면 **`null`** 반환
     */
    suspend fun findById(id: UserId): UserDto? =
        userRepository.findById(id)?.toDto()

    /**
     * 탈퇴한 사용자의 providerId를 비식별화한다.
     *
     * 소셜 로그인은 provider + providerId 조합으로 사용자를 식별하기 때문에
     * 탈퇴 후 동일한 소셜 계정으로 재가입이 가능하도록 기존 providerId를 변조한다.
     *
     * (단, 탈퇴 후 동일 소셜 계정으로의 재가입은 서비스 초기 단계에서만 허용한다.)
     *
     * 변조 형식: `DELETED_(타임스탬프)_(providerId)`
     *
     * @param userId 비식별화할 사용자 ID
     * @param providerId 현재 providerId
     *
     * @return 업데이트 성공 여부
     */
    suspend fun anonymizeProviderId(
        userId: UserId,
        providerId: String,
    ): Boolean =
        userRepository.anonymizeProviderId(userId, providerId)

    /**
     * 사용자를 비활성화한다.
     *
     * ###### 해당 메서드는 [userId] 계정을 비활성화(soft delete)하므로 주의해서 사용해야 한다.
     *
     * @param userId 비활성화할 사용자 ID
     */
    suspend fun deactivate(userId: UserId) =
        userRepository.deactivate(userId)
}
