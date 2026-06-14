package com.turnin.domain.user.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.file.domain.model.FileCategory
import com.turnin.domain.user.application.dto.UserPatchDto
import com.turnin.domain.user.domain.model.UserPatch
import com.turnin.domain.user.domain.provider.FileProvider
import com.turnin.domain.user.domain.repository.UserRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UpdateUserUseCaseTest {
    private val userRepository = mockk<UserRepository>()
    private val fileProvider = mockk<FileProvider>()
    private val usecase = UpdateUserUseCase(userRepository, fileProvider)

    @Test
    fun `성공 테스트`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, TestUserPatch)
        } returns true

        // when
        val result = usecase(TestUserId, TestUserPatchDto)

        // then
        assertTrue(result)
    }

    @Test
    fun `기존 프로필 사진이 null인 경우 파일 삭제를 수행하지 않는다`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, any())
        } returns true
        every { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) } just Runs
        val userPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = null,
            newProfileImageUrl = null,
            introduce = "introduce",
        )

        // when
        val result = usecase(TestUserId, userPatchDto)

        // then
        assertTrue(result)
        verify(exactly = 0) { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) }
    }

    @Test
    fun `기존 프로필 사진을 삭제하는 경우 기존 파일을 삭제한다`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, any())
        } returns true
        every { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) } just Runs
        val userPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = "oldImageUrl",
            newProfileImageUrl = null,
            introduce = "introduce",
        )

        // when
        val result = usecase(TestUserId, userPatchDto)

        // then
        assertTrue(result)
        verify(exactly = 1) { fileProvider.deleteFile(userPatchDto.oldProfileImageUrl!!, FileCategory.PROFILE_IMAGE) }
    }

    @Test
    fun `기존 프로필 사진과 새로운 프로필 사진이 동일한 경우 파일 삭제를 수행하지 않는다`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, any())
        } returns true
        every { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) } just Runs
        val userPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = "oldImageUrl",
            newProfileImageUrl = "oldImageUrl",
            introduce = "introduce",
        )

        // when
        val result = usecase(TestUserId, userPatchDto)

        // then
        assertTrue(result)
        verify(exactly = 0) { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) }
    }

    @Test
    fun `기존 프로필 사진과 새로운 프로필 사진이 다른 경우 기존 파일을 삭제한다`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, any())
        } returns true
        every { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) } just Runs
        val userPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = "oldImageUrl",
            newProfileImageUrl = "newImageUrl",
            introduce = "introduce",
        )

        // when
        val result = usecase(TestUserId, userPatchDto)

        // then
        assertTrue(result)
        verify(exactly = 1) { fileProvider.deleteFile(userPatchDto.oldProfileImageUrl!!, FileCategory.PROFILE_IMAGE) }
    }

    @Test
    fun `DB 업데이트에 실패하면 파일 삭제를 수행하지 않는다`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, any())
        } returns false
        every { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) } just Runs
        val userPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = "oldImageUrl",
            newProfileImageUrl = "newImageUrl",
            introduce = "introduce",
        )

        // when
        val result = usecase(TestUserId, userPatchDto)

        // then
        assertFalse(result)
        verify(exactly = 0) { fileProvider.deleteFile(any(), FileCategory.PROFILE_IMAGE) }
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserPatchDto = UserPatchDto(
            userName = "name",
            displayId = "did",
            oldProfileImageUrl = null,
            newProfileImageUrl = null,
            introduce = "introduce",
        )
        private val TestUserPatch = UserPatch(
            userName = UserName("name"),
            displayId = DisplayId("did"),
            oldProfileImageUrl = null,
            newProfileImageUrl = null,
            introduce = Introduce("introduce"),
        )
    }
}
