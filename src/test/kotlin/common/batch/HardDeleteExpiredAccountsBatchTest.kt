package com.turnin.common.batch

import com.turnin.domain.account.application.HardDeleteExpiredAccountsUseCase
import com.turnin.domain.user.application.provider.UserDeletionSupportApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HardDeleteExpiredAccountsBatchTest {
    private val hardDeleteExpiredAccountsUseCase: HardDeleteExpiredAccountsUseCase = mockk()
    private val userDeletionSupportApi: UserDeletionSupportApi = mockk()

    private val batch = HardDeleteExpiredAccountsBatch(
        hardDeleteExpiredAccountsUseCase = hardDeleteExpiredAccountsUseCase,
        userDeletionSupportApi = userDeletionSupportApi,
    )

    @Test
    fun `run - 만료 사용자가 없으면 유스케이스가 호출되지 않는다`() = runTest {
        // given
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), any())
        } returns emptyList()

        // when
        batch.run()

        // then
        coVerify(exactly = 0) { hardDeleteExpiredAccountsUseCase(any()) }
    }

    @Test
    fun `run - 만료 사용자 수만큼 유스케이스가 호출된다`() = runTest {
        // given: 만료 사용자 3명
        val expiredUserIds = listOf(1L, 2L, 3L)
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = 0)
        } returns expiredUserIds
        coEvery {
            userDeletionSupportApi.findExpiredUsers(
                any(),
                any(),
                offset = HardDeleteExpiredAccountsBatch.CHUNK_SIZE,
            )
        } returns emptyList()
        coEvery { hardDeleteExpiredAccountsUseCase(any()) } just runs

        // when
        batch.run()

        // then
        coVerify(exactly = expiredUserIds.size) { hardDeleteExpiredAccountsUseCase(any()) }
    }

    @Test
    fun `run - 청크 단위로 반복 조회한다`() = runTest {
        // given: 1청크(100명) + 추가 1명
        val chunkSize = HardDeleteExpiredAccountsBatch.CHUNK_SIZE
        val firstChunk = (1L..chunkSize).toList()
        val secondChunk = listOf(chunkSize + 1L)
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = 0)
        } returns firstChunk
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = chunkSize)
        } returns secondChunk
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = chunkSize * 2)
        } returns emptyList()
        coEvery { hardDeleteExpiredAccountsUseCase(any()) } just runs

        // when
        batch.run()

        // then: 3번 조회 (offset 0, chunkSize, chunkSize * 2)
        coVerify(exactly = 3) { userDeletionSupportApi.findExpiredUsers(any(), any(), any()) }
        coVerify(exactly = chunkSize + 1) { hardDeleteExpiredAccountsUseCase(any()) }
    }

    @Test
    fun `run - 개별 실패 시 다음 사용자가 계속 처리된다`() = runTest {
        // given: 만료 사용자 3명, 첫 번째 사용자 삭제 실패
        val expiredUserIds = listOf(1L, 2L, 3L)
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = 0)
        } returns expiredUserIds
        coEvery {
            userDeletionSupportApi.findExpiredUsers(any(), any(), offset = 100)
        } returns emptyList()
        coEvery { hardDeleteExpiredAccountsUseCase(1L) } throws RuntimeException("delete failed")
        coEvery { hardDeleteExpiredAccountsUseCase(2L) } just runs
        coEvery { hardDeleteExpiredAccountsUseCase(3L) } just runs

        // when: 예외 없이 정상 종료
        batch.run()

        // then: 실패한 1번 포함 3명 모두 시도됨
        coVerify(exactly = 3) { hardDeleteExpiredAccountsUseCase(any()) }
    }
}
