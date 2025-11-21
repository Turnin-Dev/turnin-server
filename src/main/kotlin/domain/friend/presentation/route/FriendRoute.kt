package com.peekr.domain.friend.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.dto.AddFriendRequest
import com.peekr.domain.friend.presentation.dto.DeleteFriendRequest
import com.peekr.domain.friend.presentation.dto.FriendResponse
import com.peekr.domain.friend.presentation.dto.FriendsResponse
import com.peekr.domain.friend.presentation.dto.UpdateFriendStatusRequest
import com.peekr.domain.friend.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.friendRoutes(route: Api.V1.Friend, usecase: FriendUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Friend API"
    }) {
        // TODO: 페이지네이션 필요
        get(route.FRIENDS, { getFriendsDocs() }) {
            val userId = extractUserIdWithToken()
            val friends = usecase.getFriends(userId)
            call.respond(friends.toResponse())
        }

        post({ addFriendDocs() }) {
            val userId = extractUserIdWithToken()
            val addFriendRequest = call.receive<AddFriendRequest>()
            val friendDto = usecase.add(
                ownerId = userId,
                requesterId = addFriendRequest.requesterId,
                receiverId = addFriendRequest.receiverId,
            )
            call.respond(friendDto.toResponse())
        }

        patch(route.STATUS, { updateFriendStatusDocs() }) {
            val updateFriendStatusRequest = call.receive<UpdateFriendStatusRequest>()
            val result = usecase.updateStatus(
                userId1 = updateFriendStatusRequest.requesterId,
                userId2 = updateFriendStatusRequest.receiverId,
                status = updateFriendStatusRequest.status,
            )
            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete({ deleteFriendDocs() }) {
            val deleteFriendRequest = call.receive<DeleteFriendRequest>()
            val result = usecase.delete(
                userId1 = deleteFriendRequest.requesterId,
                userId2 = deleteFriendRequest.receiverId,
            )
            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun RouteConfig.getFriendsDocs() {
    summary = "친구 목록 조회"
    description = "친구 목록을 조회한다."
    response {
        code(HttpStatusCode.OK) {
            description = "친구 목록"
            body<FriendsResponse> {
                example("FriendsResponse") {
                    value = FriendsResponse.sample
                }
            }
        }
    }
}

private fun RouteConfig.addFriendDocs() {
    summary = "친구 추가"
    description = "친구 추가"
    request {
        body<AddFriendRequest> {
            description = "친구 추가 요청 바디"
            example("AddFriendRequest") {
                value = AddFriendRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            description = "친구 응답 바디"
            body<FriendResponse> {
                example("FriendResponse") {
                    value = FriendResponse.sample
                }
            }
        }
        code(HttpStatusCode.Forbidden) {
            description = "요청자 ID와 실제 요청을 한 사용자가 같지 않은 경우"
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자가 존재하지 않는 경우"
        }
        code(HttpStatusCode.Conflict) {
            description = "이미 친구 요청을 했거나 친구 상태인 경우"
        }
    }
}

private fun RouteConfig.updateFriendStatusDocs() {
    summary = "친구 상태 수정"
    description = "친구 상태를 수정한다."
    request {
        body<UpdateFriendStatusRequest> {
            description = "친구 상태 수정 요청 바디"
            example("UpdateFriendStatusRequest") {
                value = UpdateFriendStatusRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "친구 상태 수정 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "친구 상태 수정 실패 시 혹은 수정 대상 사용자를 찾지 못하는 경우"
        }
    }
}

private fun RouteConfig.deleteFriendDocs() {
    summary = "친구 삭제"
    description = "친구를 삭제한다."
    request {
        body<DeleteFriendRequest> {
            description = "친구 삭제 요청 바디"
            example("DeleteFriendRequest") {
                value = DeleteFriendRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "친구 삭제 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "친구 삭제 실패 시 혹은 삭제 대상 사용자를 찾지 못하는 경우"
        }
    }
}
