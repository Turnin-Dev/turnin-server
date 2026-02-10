package com.peekr.domain.block.application.usecase

import com.peekr.common.db.suspendTransaction
import com.peekr.domain.block.application.dto.BlockDetailDto
import com.peekr.domain.block.application.dto.toDomain
import com.peekr.domain.block.domain.provider.FriendProvider
import com.peekr.domain.block.domain.repository.BlockRepository

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
    ) = suspendTransaction {
        // 1) 차단 생성
        val blockDetail = blockDetailDto.toDomain()
        blockRepository.createBlock(blockDetail)

        // 2) 친구 삭제
        friendProvider.deleteFriend(
            userId1 = blockDetail.blockerId,
            userId2 = blockDetail.blockedId,
        )

        // 3) 추가 연쇄 작업이 있다면 여기에 추가
        // 예: 알림 삭제, 캐시 무효화, 이벤트 발행 등
    }
}
