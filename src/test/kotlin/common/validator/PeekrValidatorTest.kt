package com.peekr.common.validator

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.validator.PeekrValidator.validation
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow

class PeekrValidatorTest {
    @Test
    fun `validation 성공 테스트`() {
        assertDoesNotThrow {
            validation(true) { "should not fail" }
        }
    }

    @Test
    fun `validation 실패 테스트`() {
        val errorMessage = "should fail"
        val exception = assertFailsWith<ValidatorException> {
            validation(false) { errorMessage }
        }

        assertEquals(errorMessage, exception.message)
    }

    @Test
    fun `validation 예외 테스트`() {
        val errorMessage = "exception occurred"
        val exception = assertFailsWith<ApiException> {
            validation(false) {
                throw NullPointerException(errorMessage)
            }
        }

        assertEquals(CommonErrorCode.Unexpected, exception.errorCode)
        assertTrue(exception.message.contains(errorMessage))
    }
}
