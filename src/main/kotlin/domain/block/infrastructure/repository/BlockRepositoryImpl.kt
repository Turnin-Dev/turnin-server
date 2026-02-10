package com.peekr.domain.block.infrastructure.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlocksPagingData
import com.peekr.domain.block.domain.repository.BlockRepository
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toDomain
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toDomainBlockReason
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll

class BlockRepositoryImpl : BlockRepository {
    override suspend fun getBlockReasons(): List<BlockReason> = suspendTransaction {
        BlockReasons
            .selectAll()
            .map { it.toDomainBlockReason() }
    }

    override suspend fun createBlock(blockDetail: BlockDetail): Unit = suspendTransaction {
        BlockEntity.new {
            this.blockerId = EntityID(blockDetail.blockerId.value, Users)
            this.blockedId = EntityID(blockDetail.blockedId.value, Users)
            this.reasonId = EntityID(blockDetail.reasonId.value, BlockReasons)
            this.customReason = blockDetail.customReason
        }
    }

    override suspend fun getBlocksById(
        userId: UserId,
        offset: Long,
        size: Int,
    ): BlocksPagingData = suspendTransaction {
        // 1) 차단 요청한 사용자가 'userId'인 경우만 조회 하도록 쿼리 구성
        val condition = Op.build {
            (Blocks.blockerId eq userId.value)
        }

        // 2) 전체 항목(차단) 개수 조회
        val totalCount = Blocks
            .select(Blocks.id)
            .where(condition)
            .count()

        // 3) 현재 페이지 목록 조회
        val blocks = Blocks
            .selectAll()
            .where(condition)
            .orderBy(Blocks.id to SortOrder.DESC)
            .limit(count = size)
            .offset(start = offset)
            .map { it.toDomain() }

        // 4) 결과 반환
        BlocksPagingData(totalCount, blocks)
    }
}
