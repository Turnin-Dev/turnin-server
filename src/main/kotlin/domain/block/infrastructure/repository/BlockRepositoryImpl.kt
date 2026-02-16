package com.peekr.domain.block.infrastructure.repository

import com.peekr.common.db.schema.BlockEntity
import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlockedUsersPagingData
import com.peekr.domain.block.domain.repository.BlockRepository
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toBlockUser
import com.peekr.domain.block.infrastructure.mapper.BlockMapper.toDomainBlockReason
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class BlockRepositoryImpl : BlockRepository {
    override suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean = suspendTransaction {
        if (userId1 == userId2) return@suspendTransaction false

        Blocks
            .select(intLiteral(1))
            .where {
                (Blocks.blockerId eq userId1.value and (Blocks.blockedId eq userId2.value)) or
                    (Blocks.blockerId eq userId2.value and (Blocks.blockedId eq userId1.value))
            }.limit(1)
            .any()
    }

    override suspend fun getBlockReasons(): List<BlockReason> = suspendTransaction {
        BlockReasons
            .selectAll()
            .map { it.toDomainBlockReason() }
    }

    override suspend fun createBlock(blockDetail: BlockDetail): BlockEntity = suspendTransaction {
        BlockEntity.new {
            this.blockerId = EntityID(blockDetail.blockerId.value, Users)
            this.blockedId = EntityID(blockDetail.blockedId.value, Users)
            this.reasonId = EntityID(blockDetail.reasonId.value, BlockReasons)
            this.customReason = blockDetail.customReason
        }
    }

    override suspend fun getBlockedUsersById(
        userId: UserId,
        offset: Long,
        size: Int,
    ): BlockedUsersPagingData = suspendTransaction {
        // 1) 조건 정의 (내가(userId) 차단한 사람들)
        val condition = Blocks.blockerId eq userId.value

        // 2) 현재 페이지 목록 조회 (size + 1 개를 조회해서 다음 페이지 존재 여부 확인)
        val join = Blocks
            .innerJoin(
                otherTable = Users,
                onColumn = { Blocks.blockedId },
                otherColumn = { Users.id },
            )
        val blocksWithExtra = join
            .select(
                Blocks.id,
                Users.id,
                Users.displayId,
                Users.name,
                Users.profileImageUrl,
            ).where(condition)
            .orderBy(Blocks.id to SortOrder.DESC)
            .limit(count = size + 1)
            .offset(offset)
            .map { it.toBlockUser() }

        // 3) 다음 페이지 존재 여부 판단
        val hasNext = blocksWithExtra.size > size

        // 4) 실제 반환한 리스트
        val blocks = if (hasNext) {
            blocksWithExtra.take(size)
        } else {
            blocksWithExtra
        }

        // 5) 결과 반환
        BlockedUsersPagingData(hasNext, blocks)
    }

    override suspend fun deleteBlock(
        ownerId: UserId,
        blockId: BlockId,
    ): Boolean = suspendTransaction {
        Blocks.deleteWhere {
            (Blocks.id eq blockId.value) and
                (Blocks.blockerId eq ownerId.value)
        } > 0
    }
}
