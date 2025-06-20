package com.peekr.common.exception

import kotlinx.serialization.Serializable

@Serializable
data class TestRequest(val value: String)

object ExceptionTestDoubles {
    val MockEmptyTestRequest = TestRequest("")
    val MockNotEmptyTestRequest = TestRequest("not empty")
}
