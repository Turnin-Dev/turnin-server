package com.turnin.domain.block.application.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.repository.BlockRepository

/**
 * 외부에 제공할 Block 삭제 제공 API
 */
class BlockDeletionSupportApi(private val blockRepository: BlockRepository) {
    /**
     * 사용자의 모든 차단 관계를 삭제한다.
     *
     * ###### 해당 메서드는 [userId]의 모든 차단 데이터를 지우므로 주의해서 사용해야 한다.
     *
     * @param userId 삭제할 사용자 ID
     */
    suspend fun deleteAll(userId: UserId) =
        blockRepository.deleteAll(userId)
}
