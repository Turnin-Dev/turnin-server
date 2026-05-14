package com.turnin.domain.notification.di

import com.turnin.domain.notification.application.provider.NotificationDeletionSupportApi
import com.turnin.domain.notification.application.provider.NotificationProviderApi
import com.turnin.domain.notification.application.usecase.DeactivateAllFcmTokensUseCase
import com.turnin.domain.notification.application.usecase.DeactivateFcmTokenUseCase
import com.turnin.domain.notification.application.usecase.DeleteAllFcmTokensUseCase
import com.turnin.domain.notification.application.usecase.GetNotificationsUseCase
import com.turnin.domain.notification.application.usecase.MarkAsReadUseCase
import com.turnin.domain.notification.application.usecase.NotificationUseCases
import com.turnin.domain.notification.application.usecase.RegisterFcmTokenUseCase
import com.turnin.domain.notification.application.usecase.SendBroadcastUseCase
import com.turnin.domain.notification.application.usecase.SendNotificationUseCase
import com.turnin.domain.notification.domain.repository.FcmTokenRepository
import com.turnin.domain.notification.domain.repository.NotificationRepository
import com.turnin.domain.notification.infrastructure.repository.FcmTokenRepositoryImpl
import com.turnin.domain.notification.infrastructure.repository.NotificationRepositoryImpl
import org.koin.dsl.module

val notificationModule = module {
    // ------------------------------ Provider ------------------------------
    single { NotificationProviderApi(get(), get(), get()) }
    single { NotificationDeletionSupportApi(get(), get()) }

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
