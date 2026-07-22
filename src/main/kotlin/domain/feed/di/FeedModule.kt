package com.turnin.domain.feed.di

import com.turnin.domain.feed.application.usecase.FeedUseCases
import com.turnin.domain.feed.application.usecase.GetAllFeedsUseCase
import com.turnin.domain.feed.application.usecase.GetFriendFeedsUseCase
import com.turnin.domain.feed.domain.repository.FeedRepository
import com.turnin.domain.feed.infrastructure.repository.FeedRepositoryImpl
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { FeedRepositoryImpl() }

    // UseCases
    factory { GetAllFeedsUseCase(get()) }
    factory { GetFriendFeedsUseCase(get()) }
    single<FeedUseCases> { FeedUseCases(get(), get()) }
}
