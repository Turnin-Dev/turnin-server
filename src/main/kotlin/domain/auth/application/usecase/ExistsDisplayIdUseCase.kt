package com.turnin.domain.auth.application.usecase

import com.turnin.common.model.id.DisplayId
import com.turnin.domain.auth.domain.repository.AuthRepository

class ExistsDisplayIdUseCase(private val authRepository: AuthRepository) {
    /**
     * 사용자 표시 ID의 존재 여부를 확인한다.
     *
     * @param displayId 사용자 표시 ID
     *
     * @return 존재하면 `true`, 존재하지 않으면 `false`
     */
    suspend operator fun invoke(displayId: DisplayId): Boolean =
        authRepository.existsByDisplayId(displayId)
}
