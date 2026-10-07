package com.turnin.domain.notification.application.provider

import com.turnin.common.firebase.FcmMessage
import com.turnin.common.firebase.FcmService
import com.turnin.common.model.NotificationType
import com.turnin.domain.notification.application.usecase.SendNotificationUseCase
import com.turnin.domain.notification.domain.repository.FcmTokenRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class NotificationProviderApiTest {
    private val sendNotification = mockk<SendNotificationUseCase>()
    private val fcmService = mockk<FcmService>()
    private val fcmTokenRepository = mockk<FcmTokenRepository>()
    private val providerApi = NotificationProviderApi(sendNotification, fcmService, fcmTokenRepository)

    @Test
    fun `토큰 목록으로 알림 전송 시 발신자 ID를 참조 소유자 키와 레거시 사용자 키에 함께 담아 전송한다`() = runTest {
        // given
        val fcmMessage = slot<FcmMessage>()
        coEvery { fcmService.sendToUsers(listOf("token1"), capture(fcmMessage)) } just Runs

        // when
        providerApi.sendNotificationToTokens(
            tokens = listOf("token1"),
            notiType = NotificationType.NEW_KEYWORD,
            title = "새 키워드",
            message = "친구가 새 키워드를 등록했어요.",
            refId = 3L,
            refType = "KEYWORD",
            senderUserId = 34L,
        )

        // then
        val expectedData = mapOf(
            "noti_type" to "NEW_KEYWORD",
            "ref_type" to "KEYWORD",
            "ref_id" to "3",
            "ref_owner_id" to "34",
            "user_id" to "34",
        )
        assertEquals(expectedData, fcmMessage.captured.data)
    }
}
