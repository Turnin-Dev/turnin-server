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
        const val ROUTE = "/V1"

        object Auth {
            const val ROUTE = "/auth"
            const val TAG = "Auth"
            const val LOGIN = "/login"
            const val REGISTER = "/register"
            const val REFRESH = "/refresh"
        }

        object User {
            const val ROUTE = "/user"
            const val TAG = "User"
            const val BY_ID = "/{id}"
        }
    }

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
