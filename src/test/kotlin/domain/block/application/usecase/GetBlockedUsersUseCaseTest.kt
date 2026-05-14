package com.turnin.domain.block.application.usecase

import com.turnin.common.model.UserName
import com.turnin.common.model.id.BlockId
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.block.application.dto.toDto
import com.turnin.domain.block.domain.model.BlockedUser
import com.turnin.domain.block.domain.repository.BlockRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBlockedUsersUseCaseTest {
    private val blockRepository = mockk<BlockRepository>()
    private val usecase = GetBlockedUsersUseCase(blockRepository)

    @Test
    fun `차단 목록 조회 성공 테스트`() = runTest {
        // given
        val userId = UserId(1L)
        val pageSize = 10
        val blockedUsersWithOneExtra = List(pageSize + 1) {
            BlockedUser(
                id = BlockId(it + 1L),
                userId = UserId(it + 1L),
                displayId = DisplayId("did${it + 1L}"),
                name = UserName("name${it + 1L}"),
                profileImageUrl = "image${it + 1L}",
            )
        }
        coEvery {
            blockRepository.getBlockedUsersById(
                userId = userId,
                cursor = null,
                size = pageSize,
            )
        } returns blockedUsersWithOneExtra

        // when
        val cursorPage = usecase(userId.value, null, pageSize)

        // then
        assertEquals(pageSize, cursorPage.items.size)
        assertEquals(
            blockedUsersWithOneExtra.take(pageSize).map { it.toDto() },
            cursorPage.items,
        )
        assertEquals(
            blockedUsersWithOneExtra
                .take(pageSize)
                .last()
                .id.value,
            cursorPage.nextCursor,
        )
    }
}
