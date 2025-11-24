package com.peekr.domain.user.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.model.FriendshipStatus
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.domain.user.UserTestDoubles.MockUserDto
import com.peekr.domain.user.application.dto.MyProfileDto
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.application.dto.UserProfileDto
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.presentation.dto.IntroducePatchRequest
import com.peekr.domain.user.presentation.dto.UserPatchRequest
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPatchEndpoint
import com.peekr.util.testPlugin
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Test

class UserRoutesTest {
    private val route = Api.V1.User
    private val userUseCases = mockk<UserUseCases>()

    @Test
    fun `사용자 조회 GET 요청 성공 테스트`() = testApplication {
        coEvery { userUseCases.get(TestMyUserId) } returns MockUserDto

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    MockUserDto.name.value,
                    MockUserDto.displayId.value,
                    MockUserDto.role.name,
                )
            },
        )
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 -잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { userUseCases.get(TestMyUserId) } returns MockUserDto

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery { userUseCases.get(TestMyUserId) } returns null

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
            responseValidator = {
                contains(HttpStatusCode.NotFound.value.toString())
            },
        )
    }

    @Test
    fun `나의 프로필 조회 GET 요청 성공 테스트`() = testApplication {
        coEvery { userUseCases.getMyProfile(TestMyUserId) } returns TestMyProfileDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.MY_PROFILE}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestMyProfileDto.displayId.value,
                    TestMyProfileDto.displayId.value,
                    TestMyProfileDto.friendsCount.toString(),
                )
            },
        )
    }

    @Test
    fun `나의 프로필 조회 GET 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { userUseCases.getMyProfile(TestMyUserId) } returns TestMyProfileDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.MY_PROFILE}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `나의 프로필 조회 GET 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery { userUseCases.getMyProfile(TestMyUserId) } returns null

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.MY_PROFILE}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `나의 프로필 조회 GET 요청 실패 테스트 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery { userUseCases.getMyProfile(TestMyUserId) } returns null

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.MY_PROFILE}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `나의 프로필 조회 GET 요청 실패 테스트 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery { userUseCases.getMyProfile(TestMyUserId) } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.MY_PROFILE}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `사용자 프로필 조회 GET 요청 성공 테스트`() = testApplication {
        coEvery {
            userUseCases.getUserProfile(TestMyUserId, TestUserId.value)
        } returns TestUserProfileDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.PROFILE}/${TestUserId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestUserProfileDto.displayId.value,
                    TestUserProfileDto.name.value,
                )
            },
        )
    }

    @Test
    fun `사용자 프로필 조회 GET 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery {
            userUseCases.getUserProfile(TestMyUserId, TestUserId.value)
        } returns TestUserProfileDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.PROFILE}/${TestUserId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `사용자 프로필 조회 GET 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery {
            userUseCases.getUserProfile(TestMyUserId, TestUserId.value)
        } returns null

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.PROFILE}/${TestUserId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `사용자 프로필 조회 GET 요청 실패 테스트 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery {
            userUseCases.getUserProfile(TestMyUserId, TestUserId.value)
        } returns TestUserProfileDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.PROFILE}/${TestUserId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `사용자 프로필 조회 GET 요청 실패 테스트 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            userUseCases.getUserProfile(TestMyUserId, TestUserId.value)
        } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.PROFILE}/${TestUserId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `사용자 수정 PATCH 요청 성공 테스트`() = testApplication {
        coEvery { userUseCases.update(TestMyUserId, TestUserPatchDto) } returns true

        testPatchEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestUserPatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NoContent,
        )
    }

    @Test
    fun `사용자 수정 PATCH 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { userUseCases.update(TestMyUserId, TestUserPatchDto) } returns true

        testPatchEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestUserPatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `사용자 수정 PATCH 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery { userUseCases.update(TestMyUserId, TestUserPatchDto) } returns false

        testPatchEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestUserPatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `사용자 수정 PATCH 요청 실패 테스트 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery { userUseCases.update(TestMyUserId, TestUserPatchDto) } returns false

        testPatchEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestUserPatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `사용자 수정 PATCH 요청 실패 테스트 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            userUseCases.update(TestMyUserId, TestUserPatchDto)
        } throws expectedApiException

        testPatchEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestUserPatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `사용자 소개글 수정 PATCH 요청 성공 테스트`() = testApplication {
        coEvery { userUseCases.updateIntroduce(TestMyUserId, TEST_INTRODUCE) } returns true

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.INTRODUCE}",
            queryParameters = null,
            requestBody = TestIntroducePatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NoContent,
        )
    }

    @Test
    fun `사용자 소개글 수정 PATCH 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { userUseCases.updateIntroduce(TestMyUserId, TEST_INTRODUCE) } returns true

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.INTRODUCE}",
            queryParameters = null,
            requestBody = TestIntroducePatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `사용자 소개글 수정 PATCH 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        coEvery { userUseCases.updateIntroduce(TestMyUserId, TEST_INTRODUCE) } returns false

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.INTRODUCE}",
            queryParameters = null,
            requestBody = TestIntroducePatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    @Test
    fun `사용자 소개글 수정 PATCH 요청 실패 테스트 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery { userUseCases.updateIntroduce(TestMyUserId, TEST_INTRODUCE) } returns true

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.INTRODUCE}",
            queryParameters = null,
            requestBody = TestIntroducePatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `사용자 소개글 수정 PATCH 요청 실패 테스트 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            userUseCases.updateIntroduce(TestMyUserId, TEST_INTRODUCE)
        } throws expectedApiException

        testPatchEndpoint(
            endpoint = "${route.ROUTE}${route.INTRODUCE}",
            queryParameters = null,
            requestBody = TestIntroducePatchRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { userRoutes(route, userUseCases) },
                )
            },
            tokenSubject = TestMyUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    companion object {
        private val TestMyUserId = UserId(1L)
        private val TestUserId = UserId(2L)
        private const val INVALID_USER_ID = "asd"
        private val TestUserPatchDto = UserPatchDto(
            displayId = DisplayId("id"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = Introduce(TEST_INTRODUCE),
        )
        private val TestUserPatchRequest = UserPatchRequest(
            displayId = "id",
            name = "name",
            profileImageUrl = null,
            introduce = TEST_INTRODUCE,
        )
        private val TestMyProfileDto = MyProfileDto(
            displayId = DisplayId("id"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = Introduce(TEST_INTRODUCE),
            isActive = true,
            lastLoginAt = 1000,
            friendsCount = 2,
        )
        private const val TEST_INTRODUCE = "test introduce"
        private val TestIntroducePatchRequest = IntroducePatchRequest(
            introduce = TEST_INTRODUCE,
        )
        private val TestUserProfileDto = UserProfileDto(
            displayId = DisplayId("honggd"),
            name = Name("honggd"),
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = Introduce("hello world!"),
            isActive = true,
            lastLoginAt = 1697875200000L,
            friendsCount = 51L,
            friendshipStatus = FriendshipStatus.NOTHING,
        )
    }
}
