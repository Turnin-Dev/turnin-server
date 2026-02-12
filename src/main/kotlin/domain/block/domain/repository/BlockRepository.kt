package com.peekr.domain.block.domain.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlocksPagingData

interface BlockRepository {
    /**
     * 차단 사용자 여부 확인
     *
     * [userId1]과 [userId2]의 차단 관계를 확인한다.
     *
     * 둘 중 한 명이라도 서로를 차단한 관계라면 `true`를 반환하고 아니라면 `false`를 반환한다.
     *
     * @param userId1 차단 관계 사용자 1 ID
     * @param userId2 차단 관계 사용자 2 ID
     */
    suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean

    /**
     * 차단 사유 목록 조회
     */
    suspend fun getBlockReasons(): List<BlockReason>

    /**
     * 차단 생성
     *
     * @param blockDetail 차단 디테일
     */
    suspend fun createBlock(blockDetail: BlockDetail): BlockEntity

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

    /**
     * 차단 삭제
     *
     * @param blockId 차단 ID
     */
    suspend fun deleteBlock(blockId: BlockId): Boolean
}
