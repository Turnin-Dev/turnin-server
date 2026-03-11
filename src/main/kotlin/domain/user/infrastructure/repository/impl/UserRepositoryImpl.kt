package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.extension.existsUser
import com.peekr.common.db.extension.filterActiveUser
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.db.updateWithTimestamp
import com.peekr.common.model.Introduce
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.PeekrDateTime
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper.toDomain
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.notExists
import org.jetbrains.exposed.sql.selectAll

class UserRepositoryImpl : UserRepository {
    override suspend fun existsUser(id: UserId): Boolean = suspendTransaction {
        Users.existsUser(id)
    }

    override suspend fun findById(id: UserId): User? = suspendTransaction {
        Users
            .selectAll()
            .where { Users.id eq id.value }
            .map { it.toDomain(false) }
            .singleOrNull()
    }

    override suspend fun findActiveById(id: UserId): User? = suspendTransaction {
        Users
            .selectAll()
            .where { Users.id eq id.value }
            .filterActiveUser()
            .map { it.toDomain(false) }
            .singleOrNull()
    }

    override suspend fun findVisibleById(
        currentId: UserId,
        id: UserId,
    ): User? = suspendTransaction {
        // 내가 상대를 차단했는지에 대한 여부
        val isBlockedByMe = exists(
            Blocks.select(intLiteral(1)).where {
                (Blocks.blockerId eq currentId.value and (Blocks.blockedId eq id.value))
            },
        )

        // 상대가 나를 차단했는지에 대한 여부
        val blockedByOtherQuery = Blocks.select(intLiteral(1)).where {
            (Blocks.blockerId eq id.value and (Blocks.blockedId eq currentId.value))
        }

        Users
            .select(Users.columns + isBlockedByMe)
            .where {
                // 상대가 나를 차단하지 않았을 때만 행을 반환하여
                // 만약 상대가 나를 차단했다면, 쿼리 결과는 0건 -> null을 반환한다.
                (Users.id eq id.value) and notExists(blockedByOtherQuery)
            }.filterActiveUser()
            .map { row ->
                // 이 매핑은 상대가 나를 차단하지 않은 상태일 때 수행
                val blockedByMe = row[isBlockedByMe]
                row.toDomain(blockedByMe)
            }.singleOrNull()
    }

    override suspend fun findByIds(ids: List<UserId>): List<User> = suspendTransaction {
        Users
            .selectAll()
            .where { Users.id inList ids.map { it.value } }
            .filterActiveUser()
            .map { it.toDomain(false) }
    }

    override suspend fun findByDisplayId(id: DisplayId): User? = suspendTransaction {
        Users
            .selectAll()
            .where { Users.displayId eq id.value }
            .filterActiveUser()
            .map { it.toDomain(false) }
            .singleOrNull()
    }

    override suspend fun update(
        userId: UserId,
        patch: UserPatch,
    ): Boolean = suspendTransaction {
        Users.updateWithTimestamp({ (Users.id eq userId.value) }) { row ->
            row[name] = patch.userName.value
            row[displayId] = patch.displayId.value
            row[profileImageUrl] = patch.newProfileImageUrl
            row[introduce] = patch.introduce.value
        } > 0
    }

    override suspend fun updateIntroduce(
        userId: UserId,
        introduce: Introduce,
    ): Boolean = suspendTransaction {
        Users.updateWithTimestamp({ (Users.id eq userId.value) }) { row ->
            row[this.introduce] = introduce.value
        } > 0
    }

    override suspend fun anonymizeProviderId(
        userId: UserId,
        providerId: String,
    ): Boolean = suspendTransaction {
        val now = PeekrDateTime.now().epochSecond
        Users.updateWithTimestamp({ Users.id eq userId.value }) {
            it[Users.providerId] = "DELETED_${now}_$providerId"
        } > 0
    }

    override suspend fun deactivate(userId: UserId): Unit = suspendTransaction {
        Users.updateWithTimestamp({ Users.id eq userId.value }) {
            // 1. 사용자 비활성화
            it[isActive] = false

            // 2. 개인 식별 정보 삭제
            it[name] = "탈퇴한 사용자"
            it[introduce] = ""
            it[profileImageUrl] = null
        }
    }
}
