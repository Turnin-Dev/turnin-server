package com.peekr.domain.block.application.usecase

import com.peekr.common.model.UserName
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.domain.model.BlockedUser
import com.peekr.domain.block.domain.model.BlockedUsersPagingData
import com.peekr.domain.block.domain.repository.BlockRepository
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
        coEvery {
            blockRepository.getBlockedUsersById(
                userId = userId,
                offset = TestPaginationParams.offset,
                size = TestPaginationParams.size,
            )
        } returns TestBlockedUsersPagingData

        // when
        val pagingDataDto = usecase(userId.value, TestPaginationParams)

        // then
        assertEquals(
            TestBlockedUsersPagingData.blockedUsers.map { it.toDto() },
            pagingDataDto.blockUsers,
        )
    }

    companion object {
        private val TestPaginationParams = PaginationParams(
            page = 1,
            size = 10,
        )
        private val TestBlockedUsersPagingData = BlockedUsersPagingData(
            hasNext = true,
            blockedUsers = listOf(
                BlockedUser(
                    id = BlockId(1L),
                    userId = UserId(2L),
                    displayId = DisplayId("did"),
                    name = UserName("name"),
                    profileImageUrl = null,
                ),
            ),
        )
    }
}
