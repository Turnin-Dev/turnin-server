package com.peekr.domain.friend.application.usecase

import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.repository.FriendRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteFriendUseCaseTest {
    private val friendRepository: FriendRepository = mockk()
    private val usecase = DeleteFriendUseCase(friendRepository)

    @Test
    fun `친구 삭제 성공 테스트`() = runTest {
        // given
        coEvery {
            friendRepository.deleteFriend(TestUserId1, TestUserId2)
        } returns true

        // when
        val result = usecase(TestUserId1.value, TestUserId2.value)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId1 = UserId(1L)
        private val TestUserId2 = UserId(2L)
    }
}
