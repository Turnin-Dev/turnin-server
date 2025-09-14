package com.peekr.domain.file.presentation.route

import com.peekr.common.route.Api
import com.peekr.domain.file.application.dto.UploadFileInfoDto
import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.exception.FileException
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class FileRouteTest {
    private val route = Api.V1.File
    private val fileUseCase = mockk<FileUseCase>()

    @Test
    fun `파일 업로드를 위한 객체를 반환받는 GET 요청 성공 테스트`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { fileUseCase.createPresignedUrl(any(), any()) } returns mockUploadFileInfoDto

        testPlugin(
            routing = { fileRoutes(route, fileUseCase) },
        )

        // when
        val response = client.get("${route.ROUTE}${route.UPLOAD}") {
            url {
                parameters.append("fileName", MOCK_VALID_FILE_NAME)
                parameters.append("mime", MOCK_VALID_MIME)
            }
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(mockUploadFileInfoDto.presignedUrl))
        assertTrue(responseBody.contains(mockUploadFileInfoDto.method))
        assertTrue(responseBody.contains(mockUploadFileInfoDto.expiresInSeconds.toString()))
    }

    @Test
    fun `파일 업로드를 위한 객체를 반환받는 GET 요청 실패 테스트 - 알려진 예외가 발생하는 경우`() = testApplication {
        // given
        val expectedException = FileException.InvalidS3PresignerArgument()
        val client = createTestClient()
        coEvery {
            fileUseCase.createPresignedUrl(any(), any())
        } throws expectedException

        testPlugin(
            routing = { fileRoutes(route, fileUseCase) },
        )

        // when
        val response = client.get("${route.ROUTE}${route.UPLOAD}") {
            url {
                parameters.append("fileName", MOCK_VALID_FILE_NAME)
                parameters.append("mime", MOCK_VALID_MIME)
            }
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.message))
    }

    @Test
    fun `파일 업로드를 위한 객체를 반환받는 GET 요청 실패 테스트 - 파일이름 유효성 검사 실패 시 상태코드 BadRequest를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { fileUseCase.createPresignedUrl(any(), any()) } returns mockUploadFileInfoDto

        testPlugin(
            routing = { fileRoutes(route, fileUseCase) },
        )

        // when
        val response = client.get("${route.ROUTE}${route.UPLOAD}") {
            url {
                parameters.append("fileName", MOCK_INVALID_FILE_NAME)
                parameters.append("mime", MOCK_VALID_MIME)
            }
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `파일 업로드를 위한 객체를 반환받는 GET 요청 실패 테스트 - MIME 유효성 검사 실패 시 상태코드 BadRequest를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { fileUseCase.createPresignedUrl(any(), any()) } returns mockUploadFileInfoDto

        testPlugin(
            routing = { fileRoutes(route, fileUseCase) },
        )

        // when
        val response = client.get("${route.ROUTE}${route.UPLOAD}") {
            url {
                parameters.append("fileName", MOCK_VALID_FILE_NAME)
                parameters.append("mime", MOCK_INVALID_MIME)
            }
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    companion object {
        private val mockUploadFileInfoDto = UploadFileInfoDto(
            presignedUrl = "presigned-url",
            method = "PUT",
            expiresInSeconds = 600,
        )
        private const val MOCK_VALID_FILE_NAME = "sample.jpg"
        private const val MOCK_INVALID_FILE_NAME = "@@@.jpg"
        private const val MOCK_VALID_MIME = "image/jpeg"
        private const val MOCK_INVALID_MIME = "video/mp4"
    }
}
