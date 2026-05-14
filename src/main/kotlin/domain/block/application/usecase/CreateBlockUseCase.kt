package com.turnin.domain.block.application.usecase

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.BlockReasonId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.domain.block.application.dto.BlockDetailDto
import com.turnin.domain.block.domain.model.BlockDetail
import com.turnin.domain.block.domain.provider.FriendProvider
import com.turnin.domain.block.domain.repository.BlockRepository

/**
 * 차단 생성
 *
 * @see invoke
 */
class CreateBlockUseCase(
    private val blockRepository: BlockRepository,
    private val friendProvider: FriendProvider,
) {
    /**
     * 차단 생성
     *
     * @param blockDetailDto 차단 디테일 DTO
     */
    suspend operator fun invoke(
        blockDetailDto: BlockDetailDto,
    ) {
        LOGGER.info(
            message = "User block attempt: " +
                "blocker=${blockDetailDto.blockerId}, blocked=${blockDetailDto.blockedId}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.BLOCK_ATTEMPT.value,
                LogTag.USER_ID.key to blockDetailDto.blockerId.toString(),
            ),
        )

        suspendTransaction {
            // 1) 차단 생성
            val blockDetail = BlockDetail.create(
                blockerId = UserId(blockDetailDto.blockerId),
                blockedId = UserId(blockDetailDto.blockedId),
                reasonId = BlockReasonId(blockDetailDto.reasonId),
                customReason = blockDetailDto.customReason,
            )
            blockRepository.createBlock(blockDetail)

            // 2) 친구 삭제
            friendProvider.deleteFriend(
                userId1 = blockDetail.blockerId,
                userId2 = blockDetail.blockedId,
            )

            // 3) 추가 연쇄 작업이 있다면 여기에 추가
            // 예: 알림 삭제, 캐시 무효화, 이벤트 발행 등
        }

        LOGGER.info(
            message = "User blocked successfully: blockedId=${blockDetailDto.blockedId}",
            tags = mapOf(
                LogTag.LOG_TYPE.key to LogType.PRIVACY.value,
                LogTag.ACTION.key to LogAction.BLOCK_SUCCESS.value,
                LogTag.USER_ID.key to blockDetailDto.blockerId.toString(),
                "blocked_user_id" to blockDetailDto.blockedId.toString(),
            ),
        )
    }
}

private val LOGGER = AppLoggerFactory.createLogger<CreateBlockUseCase>()
