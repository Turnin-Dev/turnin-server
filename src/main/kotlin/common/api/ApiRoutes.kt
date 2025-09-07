package com.peekr.common.api

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

    object V1 {
        const val ROUTE = "/v1"

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
        }

        object File {
            const val ROUTE = "/file"
            const val TAG = "File"
            const val UPLOAD = "/upload"
        }

        object Keyword {
            const val ROUTE = "/keyword"
            const val TAG = "Keyword"
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
