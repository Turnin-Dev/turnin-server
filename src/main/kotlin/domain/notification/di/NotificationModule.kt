package com.peekr.domain.notification.di

import com.peekr.domain.notification.application.provider.NotificationProviderApi
import com.peekr.domain.notification.application.usecase.DeactivateAllFcmTokensUseCase
import com.peekr.domain.notification.application.usecase.DeactivateFcmTokenUseCase
import com.peekr.domain.notification.application.usecase.DeleteAllFcmTokensUseCase
import com.peekr.domain.notification.application.usecase.GetNotificationsUseCase
import com.peekr.domain.notification.application.usecase.MarkAsReadUseCase
import com.peekr.domain.notification.application.usecase.NotificationUseCases
import com.peekr.domain.notification.application.usecase.RegisterFcmTokenUseCase
import com.peekr.domain.notification.application.usecase.SendBroadcastUseCase
import com.peekr.domain.notification.application.usecase.SendNotificationUseCase
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import com.peekr.domain.notification.domain.repository.NotificationRepository
import com.peekr.domain.notification.infrastructure.repository.FcmTokenRepositoryImpl
import com.peekr.domain.notification.infrastructure.repository.NotificationRepositoryImpl
import org.koin.dsl.module

val notificationModule = module {
    // ------------------------------ Provider ------------------------------
    single { NotificationProviderApi(get(), get(), get()) }

    // ------------------------------ Repository ------------------------------
    single<FcmTokenRepository> { FcmTokenRepositoryImpl() }
    single<NotificationRepository> { NotificationRepositoryImpl() }

    // ------------------------------ UseCase ------------------------------
    single { RegisterFcmTokenUseCase(get()) }
    single { DeactivateFcmTokenUseCase(get()) }
    single { DeactivateAllFcmTokensUseCase(get()) }
    single { DeleteAllFcmTokensUseCase(get()) }
    single { SendNotificationUseCase(get(), get(), get()) }
    single { SendBroadcastUseCase(get(), get()) }
    single { GetNotificationsUseCase(get()) }
    single { MarkAsReadUseCase(get()) }

    single {
        NotificationUseCases(
            registerToken = get(),
            deactivateToken = get(),
            deactivateAllTokens = get(),
            deleteAllTokens = get(),
            sendNotification = get(),
            sendBroadcast = get(),
            getNotifications = get(),
            markAsRead = get(),
        )
    }
}
