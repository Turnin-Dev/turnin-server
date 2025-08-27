package com.peekr.domain.file.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.domain.file.application.usecase.FileUseCase
import com.peekr.domain.file.presentation.dto.UploadFileResponse
import com.peekr.domain.file.presentation.dto.toResponse
import com.peekr.domain.file.presentation.validation.validateFileNameAndReturn
import com.peekr.domain.file.presentation.validation.validateImageMimeAndReturn
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.fileRoutes(route: Api.V1.File, fileUseCase: FileUseCase) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "File API"
    }) {
        get(route.UPLOAD, { uploadFileDocs() }) {
            val fileNameRaw = call.request.queryParameters["fileName"]
            val mimeRaw = call.request.queryParameters["mime"]
            val fileName = fileNameRaw?.trim().validateFileNameAndReturn()
            val mime = mimeRaw?.trim().validateImageMimeAndReturn()
            val uploadFileInfoDto = try {
                fileUseCase.createPresignedUrl(fileName, mime)
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CommonErrorCode.Validation.toErrorResponse(HttpStatusCode.BadRequest),
                )
                return@get
            }
            call.response.headers.append(HttpHeaders.CacheControl, "no-store")
            call.respond(
                HttpStatusCode.OK,
                uploadFileInfoDto.toResponse(),
            )
        }
    }
}

private fun RouteConfig.uploadFileDocs() {
    summary = "업로드 URL 발급"
    description = "클라이언트가 업로드에 사용할 Presigned URL 발급"
    request {
        queryParameter<String>("fileName") {
            description = "파일 이름 (영문/숫자/._- 만 허용, 1~255자)"
            example("fileName") {
                value = "asdasd1231221.jpg"
            }
        }
        queryParameter<String>("mime") {
            description = "파일 MIME 타입 (일단은 이미지 파일만 허용)"
            example("mime") {
                value = "image/jpeg"
            }
        }
    }

    response {
        code(HttpStatusCode.OK) {
            body<UploadFileResponse> {
                example("UploadFileResponse") {
                    value = UploadFileResponse.sample
                }
            }
        }

        code(HttpStatusCode.BadRequest) {
            body<ErrorResponse> {
                example("ValidationError") {
                    value = CommonErrorCode.Validation.toErrorResponse(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
