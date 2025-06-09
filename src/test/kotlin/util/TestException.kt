package com.peekr.util

import io.ktor.http.HttpStatusCode

data class TestException(
    val code: Int,
    override val message: String,
    val status: HttpStatusCode,
) : RuntimeException(message)
