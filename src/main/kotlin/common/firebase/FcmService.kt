package com.peekr.common.firebase

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MulticastMessage
import com.peekr.common.model.NotificationType
import com.peekr.common.util.AppLoggerFactory.createLogger
import com.peekr.common.util.masking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * FCM(Firebase Cloud Messaging) 전송 서비스
 *
 * Firebase Admin SDK는 Blocking API이므로 반드시 [ioDispatcher] 위에서 실행해야 한다.
 * 전송 실패 시 예외를 throw하지 않고 로깅 후 [Boolean] 또는 Unit으로 처리한다.
 *
 * @property ioDispatcher IO 작업용 코루틴 디스패처
 */
class FcmService(private val ioDispatcher: CoroutineDispatcher) {
    private val logger = createLogger<FcmService>()

    // FCM 멀티캐스트 최대 허용 토큰 수
    private val chunkSize = 500

    /**
     * 단일 사용자에게 FCM 알림을 전송한다.
     *
     * @param message 전송할 FCM 메시지 ([FcmMessage.token] 필수)
     * @return 전송 성공 시 true, 실패 시 false
     */
    suspend fun sendToUser(message: FcmMessage): Boolean =
        withContext(ioDispatcher) {
            runCatching {
                requireNotNull(message.token) { FcmErrorCode.TokenMustBeSet.description }

                val fcmMessage = Message
                    .builder()
                    .setToken(message.token)
                    .putAllData(message.toDataMap())
                    .setAndroidConfig(buildAndroidConfig(message.notiType))
                    .build()

                FirebaseMessaging.getInstance().send(fcmMessage)
                true
            }.getOrElse { e ->
                if (e is CancellationException) throw e
                logger.error(e, "FCM 단일 전송 실패 | token=${message.token?.masking()}")
                false
            }
        }

    /**
     * 여러 사용자에게 FCM 알림을 멀티캐스트로 전송한다.
     *
     * FCM 멀티캐스트는 최대 [chunkSize]개 토큰만 허용하므로
     * 토큰 목록을 자동으로 청크 단위로 나누어 전송한다.
     * 일부 토큰 전송 실패 시 해당 토큰만 경고 로깅하고 나머지는 계속 전송한다.
     *
     * @param tokens 수신자 FCM 토큰 목록
     * @param message 전송할 FCM 메시지
     */
    suspend fun sendToUsers(
        tokens: List<String>,
        message: FcmMessage,
    ): Unit = withContext(ioDispatcher) {
        runCatching {
            if (tokens.isEmpty()) return@withContext

            tokens.chunked(chunkSize).forEach { chunk ->
                val fcmMessage = MulticastMessage
                    .builder()
                    .addAllTokens(chunk)
                    .putAllData(message.toDataMap())
                    .setAndroidConfig(buildAndroidConfig(message.notiType))
                    .build()

                val response = FirebaseMessaging.getInstance().sendEachForMulticast(fcmMessage)

                // 청크 내 개별 전송 결과 확인 — 실패 토큰만 경고 로깅
                response.responses.forEachIndexed { index, sendResponse ->
                    if (!sendResponse.isSuccessful) {
                        logger.warn(
                            "FCM 멀티캐스트 일부 실패 | token=${chunk[index].masking()}",
                            sendResponse.exception,
                        )
                    }
                }
            }
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            logger.error(e, "FCM 멀티캐스트 전송 실패 | error=${e.message}")
        }
    }

    /**
     * FCM Topic을 구독한 모든 기기에 브로드캐스트 알림을 전송한다.
     *
     * 공지사항, 이벤트 등 전체 사용자 대상 알림에 사용한다.
     * 앱 설치 시 클라이언트에서 토픽 구독이 선행되어야 한다.
     *
     * @param message 전송할 FCM 메시지 ([FcmMessage.topic] 필수)
     * @return 전송 성공 시 true, 실패 시 false
     */
    suspend fun sendToTopic(message: FcmMessage): Boolean = withContext(ioDispatcher) {
        runCatching {
            requireNotNull(message.topic) { FcmErrorCode.TopicMustBeSet.description }

            val fcmMessage = Message
                .builder()
                .setTopic(message.topic)
                .putAllData(message.toDataMap())
                .setAndroidConfig(buildAndroidConfig(message.notiType))
                .build()

            FirebaseMessaging.getInstance().send(fcmMessage)
            true
        }.getOrElse { e ->
            if (e is CancellationException) throw e
            logger.error(e, "FCM 토픽 전송 실패 | topic=${message.topic}")
            false
        }
    }

    /**
     * 알림 유형에 따라 Android 전송 우선순위를 설정한다.
     *
     * FCM HIGH priority는 즉각적인 반응이 필요한 알림에만 사용해야 한다.
     * (남용 시 Google의 배터리 최적화 정책에 의해 제재를 받을 수 있다.)
     * data-only 방식 사용 시 setDirectBootOk(true) 설정으로
     * 기기 잠금 상태에서도 알림 수신 가능하다.
     *
     * - HIGH : 즉각적인 확인이 필요한 사용자 액션 (예: 친구 요청, 친구 수락)
     * - NORMAL : 즉각적이지 않아도 되는 알림 (예: 새 키워드, 공지, 이벤트)
     *
     * @param type 알림 유형 ([NotificationType])
     */
    private fun buildAndroidConfig(type: NotificationType): AndroidConfig {
        val priority = when (type) {
            // 즉각적인 반응이 필요한 알림
            NotificationType.FRIEND_REQUEST,
            NotificationType.FRIEND_ACCEPT,
            -> AndroidConfig.Priority.HIGH

            // 즉각적이지 않아도 되는 알림
            NotificationType.NEW_KEYWORD,
            NotificationType.NOTICE,
            NotificationType.EVENT,
            -> AndroidConfig.Priority.NORMAL
        }

        return AndroidConfig
            .builder()
            .setPriority(priority)
            .setDirectBootOk(true)
            .build()
    }
}
