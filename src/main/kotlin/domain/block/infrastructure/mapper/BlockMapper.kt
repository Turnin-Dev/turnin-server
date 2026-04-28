package com.turnin.domain.block.infrastructure.mapper

import com.turnin.common.db.schema.BlockReasons
import com.turnin.common.db.schema.Blocks
import com.turnin.common.db.schema.Users
import com.turnin.common.model.UserName
import com.turnin.common.model.id.BlockId
import com.turnin.common.model.id.BlockReasonId
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.model.Block
import com.turnin.domain.block.domain.model.BlockDetail
import com.turnin.domain.block.domain.model.BlockReason
import com.turnin.domain.block.domain.model.BlockedUser
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

    fun ResultRow.toBlockUser(): BlockedUser =
        BlockedUser(
            id = BlockId(this[Blocks.id].value),
            userId = UserId(this[Users.id].value),
            displayId = DisplayId(this[Users.displayId]),
            name = UserName(this[Users.name]),
            profileImageUrl = this[Users.profileImageUrl],
        )
}
