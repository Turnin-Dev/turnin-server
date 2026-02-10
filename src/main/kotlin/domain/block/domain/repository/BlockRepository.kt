package com.peekr.domain.block.domain.repository

import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlocksPagingData

interface BlockRepository {
    /**
     * 차단 사유 목록 조회
     */
    suspend fun getBlockReasons(): List<BlockReason>

    /**
     * 차단 생성
     *
     * @param blockDetail 차단 디테일
     */
    suspend fun createBlock(blockDetail: BlockDetail)

    /**
     * 차단 목록 조회 (페이지네이션)
     *
     * @param userId 조회할 사용자 ID
     * @param offset 페이지 오프셋
     * @param size 페이지 사이즈
     */
    suspend fun getBlocksById(
        userId: UserId,
        offset: Long,
        size: Int,
    ): BlocksPagingData
}
