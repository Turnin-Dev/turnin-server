package com.peekr.domain.block.infrastructure.mapper

import com.peekr.common.db.schema.BlockReasons
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Users
import com.peekr.common.model.UserName
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.BlockReasonId
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.block.domain.model.Block
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlockReason
import com.peekr.domain.block.domain.model.BlockUser
import org.jetbrains.exposed.sql.ResultRow

object BlockMapper {
    fun ResultRow.toDomainBlockReason(): BlockReason =
        BlockReason(
            id = BlockReasonId(this[BlockReasons.id].value),
            code = this[BlockReasons.code],
            description = this[BlockReasons.description],
        )

    fun ResultRow.toDomain(): Block =
        Block(
            id = BlockId(this[Blocks.id].value),
            detail = BlockDetail(
                blockerId = UserId(this[Blocks.blockerId].value),
                blockedId = UserId(this[Blocks.blockedId].value),
                reasonId = BlockReasonId(this[Blocks.reasonId].value),
                customReason = this[Blocks.customReason],
            ),
        )

    fun ResultRow.toBlockUser(): BlockUser =
        BlockUser(
            id = BlockId(this[Blocks.id].value),
            userId = UserId(this[Users.id].value),
            displayId = DisplayId(this[Users.displayId]),
            name = UserName(this[Users.name]),
            profileImageUrl = this[Users.profileImageUrl],
        )
}
