package com.peekr.domain.block.domain.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlockedUser

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
     * 차단 사용자 목록 조회 (페이지네이션)
     *
     * 초기 호출 시 커서 값은 null이다.
     *
     * 페이지네이션을 위해 실제 조회 개수는 pageSize + 1이다.
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 커서 (차단 ID)
     * @param size 페이지 사이즈
     */
    suspend fun getBlockedUsersById(
        userId: UserId,
        cursor: Long?,
        size: Int,
    ): List<BlockedUser>

    /**
     * 차단 삭제
     *
     * @param ownerId 차단 요청한 사용자 ID
     * @param blockId 차단 ID
     */
    suspend fun deleteBlock(
        ownerId: UserId,
        blockId: BlockId,
    ): Boolean
}
