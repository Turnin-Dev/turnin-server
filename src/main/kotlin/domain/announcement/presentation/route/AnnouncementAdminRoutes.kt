package com.turnin.domain.announcement.presentation.route

import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.common.route.Api.byPathParam
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.announcement.application.usecase.AnnouncementAdminUseCases
import com.turnin.domain.announcement.presentation.dto.CreateAnnouncementRequest
import com.turnin.domain.announcement.presentation.dto.UpdateAnnouncementStatusRequest
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.announcementAdminRoutes(route: Api.Admin.Announcement, usecase: AnnouncementAdminUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Announcement Admin API"
        specName = "admin"
    }) {
        post({ createAnnouncementDocs() }) {
            val request = call.receive<CreateAnnouncementRequest>()
            usecase.create(request.toDto())
            call.respond(HttpStatusCode.Created)
        }

        patch(route.STATUS.byPathParam("announcementId"), { updateAnnouncementStatusDocs() }) {
            val announcementId = call.parameters["announcementId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("공지 ID")
            val request = call.receive<UpdateAnnouncementStatusRequest>()
            usecase.update(AnnouncementId(announcementId), request.status)
            call.respond(HttpStatusCode.OK)
        }

        delete("".byPathParam("announcementId"), { deleteAnnouncementDocs() }) {
            val announcementId = call.parameters["announcementId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("공지 ID")
            usecase.delete(AnnouncementId(announcementId))
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun RouteConfig.createAnnouncementDocs() {
    summary = "공지 생성"
    description = "공지를 생성한다. 생성된 공지는 기본적으로 비활성 상태이다."
    request {
        body<CreateAnnouncementRequest> {
            description = "공지 생성 요청 바디"
            example("CreateAnnouncementRequest") {
                value = CreateAnnouncementRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            description = "공지 생성 성공 시"
        }
    }
}

private fun RouteConfig.updateAnnouncementStatusDocs() {
    summary = "공지 상태 변경"
    description = "공지 상태를 변경한다. (활성/비활성)"
    request {
        pathParameter<Long>("id") {
            description = "공지 ID"
        }
        body<UpdateAnnouncementStatusRequest> {
            description = "공지 상태 변경 요청 바디"
            example("UpdateAnnouncementStatusRequest") {
                value = UpdateAnnouncementStatusRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "상태 변경 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "공지가 존재하지 않는 경우"
        }
    }
}

private fun RouteConfig.deleteAnnouncementDocs() {
    summary = "공지 삭제"
    description = "공지를 삭제한다."
    request {
        pathParameter<Long>("id") {
            description = "공지 ID"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "공지 삭제 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "공지가 존재하지 않는 경우"
        }
    }
}
