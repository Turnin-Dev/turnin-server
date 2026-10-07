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
    fun `run - 청크 삭제에 실패하면 청크의 키워드를 하나씩 다시 삭제한다`() = runTest {
        // given: 키워드 3개 청크 삭제 실패, 하나씩 삭제는 성공
        val chunk = listOf(1L, 2L, 3L)
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = null)
        } returns chunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = 3L)
        } returns emptyList()
        coEvery { hardDeleteExpiredUserKeywordsUseCase(any()) } just runs
        coEvery { hardDeleteExpiredUserKeywordsUseCase(chunk) } throws RuntimeException("delete failed")

        // when
        batch.run()

        // then
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(listOf(1L)) }
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(listOf(2L)) }
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(listOf(3L)) }
    }

    @Test
    fun `run - 하나씩 다시 삭제할 때 실패한 키워드만 건너뛰고 나머지는 삭제한다`() = runTest {
        // given: 청크 삭제 실패, 2번 키워드는 하나씩 삭제해도 실패
        val chunk = listOf(1L, 2L, 3L)
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = null)
        } returns chunk
        coEvery {
            userKeywordDeletionSupportApi.findIdsDeletedBefore(any(), any(), afterId = 3L)
        } returns emptyList()
        coEvery { hardDeleteExpiredUserKeywordsUseCase(any()) } just runs
        coEvery { hardDeleteExpiredUserKeywordsUseCase(chunk) } throws RuntimeException("delete failed")
        coEvery { hardDeleteExpiredUserKeywordsUseCase(listOf(2L)) } throws RuntimeException("delete failed")

        // when: 예외 없이 정상 종료
        batch.run()

        // then: 2번 이후의 3번 키워드도 삭제를 시도한다
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(listOf(3L)) }
    }

    @Test
    fun `run - 청크 삭제에 실패해도 다음 청크가 계속 처리된다`() = runTest {
        // given: 첫 번째 청크와 그 키워드 전부 삭제 실패
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
        coEvery { hardDeleteExpiredUserKeywordsUseCase(any()) } throws RuntimeException("delete failed")
        coEvery { hardDeleteExpiredUserKeywordsUseCase(secondChunk) } just runs

        // when: 예외 없이 정상 종료
        batch.run()

        // then
        coVerify(exactly = 1) { hardDeleteExpiredUserKeywordsUseCase(secondChunk) }
    }
}
