package com.turnin.common.batch

import com.turnin.domain.account.application.HardDeleteExpiredUserKeywordsUseCase
import com.turnin.domain.userKeyword.application.provider.UserKeywordDeletionSupportApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HardDeleteExpiredUserKeywordsBatchTest {
    private val hardDeleteExpiredUserKeywordsUseCase: HardDeleteExpiredUserKeywordsUseCase = mockk()
    private val userKeywordDeletionSupportApi: UserKeywordDeletionSupportApi = mockk()

    private val batch = HardDeleteExpiredUserKeywordsBatch(
        hardDeleteExpiredUserKeywordsUseCase = hardDeleteExpiredUserKeywordsUseCase,
        userKeywordDeletionSupportApi = userKeywordDeletionSupportApi,
    )

    @Test
    fun `run - 만료된 키워드가 없으면 유스케이스가 호출되지 않는다`() = runTest {
        // given
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = null)
        } returns emptyList()

        // when
        batch.run()

        // then
        coVerify(exactly = 0) { hardDeleteExpiredUserKeywordsUseCase(any()) }
    }

    @Test
    fun `run - 청크 단위로 조회한 키워드 ID 목록으로 유스케이스를 호출한다`() = runTest {
        // given: 1청크(100개) + 추가 1개
        val chunkSize = HardDeleteExpiredUserKeywordsBatch.CHUNK_SIZE
        val firstChunk = (1L..chunkSize).toList()
        val secondChunk = listOf(chunkSize + 1L)
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = null)
        } returns firstChunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = chunkSize.toLong())
        } returns secondChunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = chunkSize + 1L)
        } returns emptyList()
        coEvery { hardDeleteExpiredUserKeywordsUseCase(any()) } just runs

        // when
        batch.run()

        // then
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(firstChunk) }
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(secondChunk) }
    }

    @Test
    fun `run - 청크 삭제에 실패해도 다음 청크가 계속 처리된다`() = runTest {
        // given: 첫 번째 청크 삭제 실패
        val chunkSize = HardDeleteExpiredUserKeywordsBatch.CHUNK_SIZE
        val firstChunk = (1L..chunkSize).toList()
        val secondChunk = listOf(chunkSize + 1L)
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = null)
        } returns firstChunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = chunkSize.toLong())
        } returns secondChunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = chunkSize + 1L)
        } returns emptyList()
        coEvery { hardDeleteExpiredUserKeywordsUseCase(firstChunk) } throws RuntimeException("delete failed")
        coEvery { hardDeleteExpiredUserKeywordsUseCase(secondChunk) } just runs

        // when: 예외 없이 정상 종료
        batch.run()

        // then
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(secondChunk) }
    }
}
