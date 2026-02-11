package com.peekr.domain.block.application.usecase

import com.peekr.common.model.id.BlockId
import com.peekr.common.model.id.BlockReasonId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.pagination.offset.PaginationParams
import com.peekr.domain.block.application.dto.toDto
import com.peekr.domain.block.domain.model.Block
import com.peekr.domain.block.domain.model.BlockDetail
import com.peekr.domain.block.domain.model.BlocksPagingData
import com.peekr.domain.block.domain.repository.BlockRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBlocksUseCaseTest {
    private val blockRepository = mockk<BlockRepository>()
    private val usecase = GetBlocksUseCase(blockRepository)

    @Test
    fun `차단 목록 조회 성공 테스트`() = runTest {
        // given
        val userId = UserId(1L)
        coEvery {
            blockRepository.getBlocksById(
                userId = userId,
                offset = TestPaginationParams.offset,
                size = TestPaginationParams.size,
            )
        } returns TestBlocksPagingData

        // when
        val pagingDataDto = usecase(userId.value, TestPaginationParams)

        // then
        assertEquals(TestBlocksPagingData.totalSize, pagingDataDto.pagingData.totalSize)
        assertEquals(
            TestBlocksPagingData.blocks.map { it.toDto() },
            pagingDataDto.blocks,
        )
    }

    companion object {
        private val TestPaginationParams = PaginationParams(
            page = 1,
            size = 10,
        )
        private val TestBlocksPagingData = BlocksPagingData(
            totalSize = 30,
            blocks = listOf(
                Block(
                    id = BlockId(1L),
                    detail = BlockDetail(
                        blockerId = UserId(1L),
                        blockedId = UserId(2L),
                        reasonId = BlockReasonId(1L),
                        customReason = "custom-reason",
                    ),
                ),
            ),
        )
    }
}
