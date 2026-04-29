package com.turnin.domain.block.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.block.application.dto.BlockedUserDto
import com.turnin.domain.block.application.dto.toDto
import com.turnin.domain.block.domain.repository.BlockRepository

/**
 * 차단 사용자 목록 조회
 *
 * @see invoke
 */
class GetBlockedUsersUseCase(private val blockRepository: BlockRepository) {
    /**
     * 차단 사용자 목록을 조회한다. (페이지네이션)
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 커서 값 (차단 ID)
     * @param pageSize 페이지 사이즈
     */
    suspend operator fun invoke(
        userId: Long,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<BlockedUserDto, Long> {
        // 0) 데이터 전처리
        val userIdVO = UserId(userId)

        // 1) 데이터 조회
        val blockedUsersWithOneExtra = blockRepository.getBlockedUsersById(userIdVO, cursor, pageSize)

        // 2) 다음 페이지 존재 여부 확인 및 다음 커서 결정
        val hasNext = blockedUsersWithOneExtra.size > pageSize
        val blockedUsers = if (hasNext) {
            blockedUsersWithOneExtra.take(pageSize)
        } else {
            blockedUsersWithOneExtra
        }
        val nextCursor = if (hasNext) blockedUsers.last().id.value else null

        // 3) 결과 반환
        return CursorPage(items = blockedUsers.map { it.toDto() }, nextCursor = nextCursor)
    }
}
