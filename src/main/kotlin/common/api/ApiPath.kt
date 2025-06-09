package com.peekr.common.api

object ApiPath {
    private const val API = "/api"

    object V1 {
        private const val VERSION = "/v1"
        const val BASE = "$API$VERSION"

        object Auth {
            const val ROOT = "/auth"
            const val LOGIN = "$ROOT/login"
            const val REGISTER = "$ROOT/register"
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
