package com.peekr.domain.friend.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.presentation.dto.AddFriendRequest
import com.peekr.domain.friend.presentation.dto.DeleteFriendRequest
import com.peekr.domain.friend.presentation.dto.UpdateFriendStatusRequest
import com.peekr.domain.friend.presentation.dto.toResponse
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
        get(route.FRIENDS, {}) {
            val userId = extractUserIdWithToken()
            val friends = usecase.getFriends(userId)
            call.respond(friends.toResponse())
        }

        post({}) {
            val userId = extractUserIdWithToken()
            val addFriendRequest = call.receive<AddFriendRequest>()
            val friendDto = usecase.add(
                ownerId = userId,
                requesterId = addFriendRequest.requesterId,
                receiverId = addFriendRequest.receiverId,
            )
            call.respond(friendDto.toResponse())
        }

        patch(route.STATUS, {}) {
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

        delete({}) {
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
