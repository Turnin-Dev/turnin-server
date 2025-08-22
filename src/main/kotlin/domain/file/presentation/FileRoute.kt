package com.peekr.domain.file.presentation

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.domain.auth.presentation.dto.ExistsResultResponse
import com.peekr.domain.file.application.usecase.FileUseCase
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.fileRoutes(route: Api.V1.File, fileUseCase: FileUseCase) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "File API"
    }) {
        get(route.UPLOAD.byPathParam("fileName"), { uploadFileDocs() }) {
            val fileName = call.request.pathVariables["fileName"]
            if (fileName.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CommonErrorCode.Validation.toErrorResponse(HttpStatusCode.BadRequest),
                )
                return@get
            }

            val presignedUrl = fileUseCase.createPresignedUrl(fileName)
            call.respond(
                HttpStatusCode.OK,
                UploadFileResponse(presignedUrl = presignedUrl),
            )
        }
    }
}

private fun RouteConfig.uploadFileDocs() {
    summary = "파일 업로드"
    description = "파일 업로드"
    request {
        pathParameter<String>("fileName") {
            description = "파일 이름"
            example("fileName") {
                value = "asdasd1231221.jpg"
            }
        }
    }

    response {
        code(HttpStatusCode.OK) {
            body<ExistsResultResponse> {
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
