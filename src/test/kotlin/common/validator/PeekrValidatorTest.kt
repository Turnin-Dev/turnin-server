package com.turnin.common.validator

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.validator.PeekrValidator.validation
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull

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
    fun `validation 예외 테스트 - IllegalArgumentException 예외 발생 시 ValidatorException 예외로 변환한다`() {
        val errorMessage = "exception occurred"
        val exception = assertFailsWith<ValidatorException> {
            validation(false) {
                throw IllegalArgumentException(errorMessage)
            }
        }

        assertNotNull(exception.message)
        assertTrue(exception.message!!.contains(errorMessage))
    }

    @Test
    fun `validation 예외 테스트 - IllegalArgumentException가 아닌 예외 발생 시 ValidationDefault 예외 발생`() {
        val errorMessage = "exception occurred"
        val exception = assertFailsWith<ApiException> {
            validation(false) {
                throw NullPointerException(errorMessage)
            }
        }

        assertEquals(CommonErrorCode.ValidationDefault.code, exception.errorCode.code)
        assertTrue(exception.message.contains(errorMessage))
    }
}
