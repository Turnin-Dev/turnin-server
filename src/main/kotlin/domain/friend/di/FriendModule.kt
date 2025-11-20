package com.peekr.domain.friend.di

import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.repository.FriendRepositoryImpl
import org.koin.dsl.module

val friendModule = module {
    single<FriendRepository> { FriendRepositoryImpl() }
}
