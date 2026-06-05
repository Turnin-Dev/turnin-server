package com.turnin.domain.announcement.presentation.route

import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.domain.announcement.application.usecase.AnnouncementUseCases
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode

fun AuthenticatedRoute.announcementRoutes(route: Api.V1.Announcement, usecase: AnnouncementUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Announcement API"
    }) {
        get({ getAnnouncementsDocs() }) {
//            val userId = extractUserIdWithToken()
//            val userRole = extractUserRoleWithToken()
//            val announcements = usecase.getAnnouncements(userId.value, userRole)
//            call.respond(HttpStatusCode.OK, announcements.toResponse())
        }

        post(route.READ, { markAnnouncementAsReadDocs() }) {
//            val announcementId = call.parameters["id"]
//                ?.toLongOrNull()
//                .inputValidationAndReturn("공지 ID")
//            val userId = extractUserIdWithToken()
//            usecase.markAsRead(AnnouncementId(announcementId), userId.value)
//            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun RouteConfig.getAnnouncementsDocs() {
    summary = "공지 목록 조회"
    description = "활성 공지 목록을 읽음 여부와 함께 조회한다."
    response {
        code(HttpStatusCode.OK) {
            description = "공지 목록"
//            body<List<AnnouncementResponse>> {
//                example("AnnouncementResponse") {
//                    value = AnnouncementResponse.sampleList
//                }
//            }
        }
    }
}

private fun RouteConfig.markAnnouncementAsReadDocs() {
    summary = "공지 읽음 처리"
    description = "공지를 읽음 처리한다. 이미 읽은 공지라면 무시한다."
    request {
        pathParameter<Long>("id") {
            description = "공지 ID"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "읽음 처리 성공 시"
        }
    }
}
