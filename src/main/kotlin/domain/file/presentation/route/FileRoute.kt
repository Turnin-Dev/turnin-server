package com.peekr.domain.file.presentation.route

import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.route.Api
import com.peekr.domain.file.application.usecase.FileUseCases
import com.peekr.domain.file.presentation.dto.UploadFileResponse
import com.peekr.domain.file.presentation.dto.toResponse
import com.peekr.domain.file.presentation.validation.validateFileNameAndReturn
import com.peekr.domain.file.presentation.validation.validateImageMimeAndReturn
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.fileRoutes(route: Api.V1.File, usecase: FileUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "File API"
    }) {
        get(route.UPLOAD, { uploadFileDocs() }) {
            val fileNameParam = call.request.queryParameters["fileName"]
            val mimeRaw = call.request.queryParameters["mime"]
            val fileName = fileNameParam?.trim().validateFileNameAndReturn()
            val mime = mimeRaw?.trim().validateImageMimeAndReturn()
            val uploadFileInfoDto = usecase.getFileUploadUrl(fileName, mime)
            call.respond(
                HttpStatusCode.OK,
                uploadFileInfoDto.toResponse(),
            )
        }

        get(route.UPDATE, { updateFileDocs() }) {
            val newFileNameParam = call.request.queryParameters["newFileName"]
            val mimeRaw = call.request.queryParameters["mime"]
            val newFileName = newFileNameParam?.trim().validateFileNameAndReturn()
            val mime = mimeRaw?.trim().validateImageMimeAndReturn()
            val uploadFileInfoDto = usecase.getFileUpdateUrl(newFileName, mime)
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
                    value = CommonErrorCode.ValidationDefault
                        .toErrorResponse(HttpStatusCode.BadRequest)
                }
                example("InvalidS3PresignerArgument") {
                    value = com.peekr.domain.file.exception.FileErrorCode.InvalidS3PresignerArgument
                        .toErrorResponse(HttpStatusCode.BadRequest)
                }
            }
        }

        code(HttpStatusCode.InternalServerError) {
            body<ErrorResponse> {
                example("S3CredentialFailed") {
                    value = com.peekr.domain.file.exception.FileErrorCode.S3CredentialFailed
                        .toErrorResponse(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

private fun RouteConfig.updateFileDocs() {
    summary = "업데이트 URL 발급"
    description = "클라이언트가 업데이트에 사용할 Presigned URL 발급"
    request {
        queryParameter<String>("newFileName") {
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

        code(HttpStatusCode.InternalServerError) {
            body<ErrorResponse> {
                example("R2DeleteFailed") {
                    value = com.peekr.domain.file.exception.FileErrorCode.R2DeleteFailed
                        .toErrorResponse(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}
