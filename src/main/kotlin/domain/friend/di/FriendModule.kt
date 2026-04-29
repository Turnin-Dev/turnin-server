package com.turnin.domain.friend.di

import com.turnin.common.di.ApplicationScopeQualifier
import com.turnin.domain.friend.application.provider.FriendDeletionSupportApi
import com.turnin.domain.friend.application.provider.FriendProviderApi
import com.turnin.domain.friend.application.usecase.AddFriendUseCase
import com.turnin.domain.friend.application.usecase.DeleteFriendUseCase
import com.turnin.domain.friend.application.usecase.FriendUseCases
import com.turnin.domain.friend.application.usecase.GetFriendStatusUseCase
import com.turnin.domain.friend.application.usecase.GetFriendsUseCase
import com.turnin.domain.friend.application.usecase.GetIncomingRequestsUseCase
import com.turnin.domain.friend.application.usecase.UpdateFriendRequestStatusUseCase
import com.turnin.domain.friend.domain.provider.NotificationProvider
import com.turnin.domain.friend.domain.repository.FriendRepository
import com.turnin.domain.friend.infrastructure.provider.NotificationProviderImpl
import com.turnin.domain.friend.infrastructure.repository.FriendRepositoryImpl
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
