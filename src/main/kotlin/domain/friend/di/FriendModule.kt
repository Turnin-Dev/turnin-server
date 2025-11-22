package com.peekr.domain.friend.di

import com.peekr.domain.friend.application.provider.FriendProviderApi
import com.peekr.domain.friend.application.usecase.AddFriendUseCase
import com.peekr.domain.friend.application.usecase.DeleteFriendUseCase
import com.peekr.domain.friend.application.usecase.FriendUseCases
import com.peekr.domain.friend.application.usecase.GetFriendsUseCase
import com.peekr.domain.friend.application.usecase.UpdateFriendStatusUseCase
import com.peekr.domain.friend.domain.provider.UserProvider
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.provider.UserProviderImpl
import com.peekr.domain.friend.infrastructure.repository.FriendRepositoryImpl
import org.koin.dsl.module

val friendModule = module {
    // Repository
    single<FriendRepository> { FriendRepositoryImpl() }

    // Provider
    single { FriendProviderApi(get()) }
    single<UserProvider> { UserProviderImpl(get()) }

    // Usecase
    factory { GetFriendsUseCase(get()) }
    factory { AddFriendUseCase(get(), get()) }
    factory { UpdateFriendStatusUseCase(get(), get()) }
    factory { DeleteFriendUseCase(get()) }
    single<FriendUseCases> {
        FriendUseCases(get(), get(), get(), get())
    }
}
