package com.turnin.domain.block.infrastructure.repository

import com.turnin.common.db.extension.filterActiveUser
import com.turnin.common.db.extension.isBlockedRelationship
import com.turnin.common.db.schema.BlockEntity
import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.BlockId
import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.model.BlockDetail
import com.turnin.domain.block.domain.model.BlockReason
import com.turnin.domain.block.domain.model.BlockedUser
import com.turnin.domain.block.domain.repository.BlockRepository
import com.turnin.domain.block.infrastructure.mapper.BlockMapper.toBlockUser
import com.turnin.domain.block.infrastructure.mapper.BlockMapper.toDomainBlockReason
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class BlockRepositoryImpl : BlockRepository {
    override suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean = suspendTransaction {
        if (userId1 == userId2) return@suspendTransaction false

        Blocks.isBlockedRelationship(userId1, userId2)
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
        cursor: Long?,
        size: Int,
    ): List<BlockedUser> = suspendTransaction {
        // 1) 조건 정의 (내가(userId) 차단한 사람들 + 커서)
        val condition = (Blocks.blockerId eq userId.value).let { base ->
            if (cursor != null) {
                base and (Blocks.id less cursor)
            } else {
                base
            }
        }

        // 2) 현재 페이지 목록 조회 (다음 페이지 존재 여부 확인을 위해 size + 1 개를 조회)
        val join = Blocks
            .innerJoin(
                otherTable = Users,
                onColumn = { Blocks.blockedId },
                otherColumn = { Users.id },
            )

        // 3) 결과 반환
        join
            .select(
                Blocks.id,
                Users.id,
                Users.displayId,
                Users.name,
                Users.profileImageUrl,
            ).where(condition)
            .filterActiveUser()
            .orderBy(Blocks.id to SortOrder.DESC)
            .limit(count = size + 1)
            .map { it.toBlockUser() }
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

    override suspend fun deleteAll(userId: UserId): Unit = suspendTransaction {
        Blocks.deleteWhere {
            (Blocks.blockerId eq userId.value) or
                (Blocks.blockedId eq userId.value)
        }
    }
}
