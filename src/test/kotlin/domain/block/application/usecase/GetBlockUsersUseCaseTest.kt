package com.peekr.domain.block.application.usecase

import com.peekr.common.model.UserName
import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.domain.model.BlockUser
import com.peekr.domain.block.domain.model.BlockUsersPagingData
import com.peekr.domain.block.domain.repository.BlockRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBlockUsersUseCaseTest {
    private val blockRepository = mockk<BlockRepository>()
    private val usecase = GetBlockUsersUseCase(blockRepository)

    @Test
    fun `차단 목록 조회 성공 테스트`() = runTest {
        // given
        val userId = UserId(1L)
        coEvery {
            blockRepository.getBlockUsersById(
                userId = userId,
                offset = TestPaginationParams.offset,
                size = TestPaginationParams.size,
            )
        } returns TestBlockUsersPagingData

        // when
        val pagingDataDto = usecase(userId.value, TestPaginationParams)

        // then
        assertEquals(
            TestBlockUsersPagingData.blockUsers.map { it.toDto() },
            pagingDataDto.blockUsers,
        )
    }

    companion object {
        private val TestPaginationParams = PaginationParams(
            page = 1,
            size = 10,
        )
        private val TestBlockUsersPagingData = BlockUsersPagingData(
            hasNext = true,
            blockUsers = listOf(
                BlockUser(
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
