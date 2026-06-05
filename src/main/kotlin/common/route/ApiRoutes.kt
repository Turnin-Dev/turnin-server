package com.turnin.common.route

/**
 * API 경로
 *
 * **`ROUTE`** : 부모 경로 명
 *
 * **`PATH(Ex. LOGIN)`** : 자식 경로 명
 *
 * **`TAG`** : 경로 그룹 명
 */
object Api {
    const val ROUTE = "/api"

    object Health {
        const val ROUTE = "/health"
        const val TAG = "Health"
        const val DETAIL = "/detail"
    }

    object V1 {
        const val ROUTE = "/v1"

        object Account {
            const val ROUTE = "/account"
            const val TAG = "Account"
        }

        object Auth {
            const val ROUTE = "/auth"
            const val TAG = "Auth"
            const val LOGIN = "/login"
            const val REGISTER = "/register"
            const val REFRESH = "/refresh"
            const val EXISTS_USER = "/exists/provider"
            const val EXISTS_DISPLAY_ID = "/exists/displayId"
        }

        object User {
            const val ROUTE = "/user"
            const val TAG = "User"
            const val INTRODUCE = "/introduce"
            const val LOGOUT = "/logout"

            fun myProfile(): String = "/me/profile"

            fun profile(pathParam: String): String = "/$pathParam/profile"
        }

        object File {
            const val ROUTE = "/file"
            const val TAG = "File"
            const val UPLOAD = "/upload"
            const val UPDATE = "/update"
        }

        object Keyword {
            const val ROUTE = "/keyword"
            const val ID = "/id"
            const val NAME = "/name"
            const val TAG = "Keyword"
        }

        object UserKeyword {
            const val ROUTE = "/user-keyword"
            const val TAG = "UserKeyword"

            fun detail(pathParam: String): String = "$ROUTE/$pathParam/detail"
        }

        object Report {
            const val ROUTE = "/report"
            const val TAG = "Report"
            const val REASON = "/reason"
        }

        object Friend {
            const val ROUTE = "/friend"
            const val TAG = "Friend"
            const val FRIENDS = "/list"
            const val INCOMING_REQUEST = "/incoming-request"
            const val STATUS = "/status"
        }

        object Discover {
            const val ROUTE = "/discover"
            const val TAG = "Discover"
        }

        object Feed {
            const val ROUTE = "/feed"
            const val TAG = "Feed"
        }

        object Block {
            const val ROUTE = "/block"
            const val TAG = "Block"
            const val REASON = "/reason"
        }

        object Notification {
            const val ROUTE = "/notification"
            const val TAG = "Notification"
            const val TOKEN = "/token"
            const val DEACTIVATE_TOKEN = "/token/deactivate"
            const val READ = "read"

            fun read(pathParam: String): String = "{$pathParam}/$READ"
        }

        object Announcement {
            const val ROUTE = "/announcement"
            const val TAG = "Announcement"
            const val READ = "/read"
        }
    }

    /** PathParameter를 사용해 키 값 이름이 포함된 경로를 반환한다. */
    fun String.byPathParam(idName: String) = "$this/{$idName}"

    /** PathParameter를 사용해 키 값 이름이 포함된 경로를 반환한다. */
    fun String.byPathParam(idName: String, idName2: String) = "$this/{$idName}/{$idName2}"

    // Samples
//    object User {
//        private const val ROOT = "$BASE/users"
//        const val ME = "$ROOT/me"
//        fun byId(id: Long) = "$ROOT/$id"  // path param
//    }
//
//    object Post {
//        private const val ROOT = "$BASE/posts"
//        const val CREATE = "$ROOT"
//        fun byId(postId: Long) = "$ROOT/$postId"
//        fun comments(postId: Long) = "$ROOT/$postId/comments"
//    }
}
