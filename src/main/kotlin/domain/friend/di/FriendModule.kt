package com.peekr.domain.friend.di

import com.peekr.common.di.ApplicationScopeQualifier
import com.peekr.domain.friend.application.provider.FriendDeletionSupportApi
import com.peekr.domain.friend.application.provider.FriendProviderApi
import com.peekr.domain.friend.application.usecase.AddFriendUseCase
import com.peekr.domain.friend.application.usecase.DeleteFriendUseCase
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.application.usecase.GetFriendStatusUseCase
import com.peekr.domain.friend.application.usecase.GetFriendsUseCase
import com.peekr.domain.friend.application.usecase.GetIncomingRequestsUseCase
import com.peekr.domain.friend.application.usecase.UpdateFriendRequestStatusUseCase
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.provider.NotificationProviderImpl
import com.peekr.domain.friend.infrastructure.repository.FriendRepositoryImpl
import org.koin.dsl.module

val friendModule = module {
    // Provider
    single<NotificationProvider> { NotificationProviderImpl(get()) }

    // Repository
    single<FriendRepository> { FriendRepositoryImpl() }

    // Provider
    single { FriendProviderApi(get(), get()) }
    single { FriendDeletionSupportApi(get()) }

    // Usecase
    single { GetFriendsUseCase(get()) }
    single { AddFriendUseCase(get(), get(), get(ApplicationScopeQualifier)) }
    single { UpdateFriendRequestStatusUseCase(get(), get(), get(ApplicationScopeQualifier)) }
    single { DeleteFriendUseCase(get()) }
    single { GetFriendStatusUseCase(get()) }
    single { GetIncomingRequestsUseCase(get()) }
    single {
        FriendUseCases(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
