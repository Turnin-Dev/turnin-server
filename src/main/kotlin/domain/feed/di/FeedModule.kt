package com.peekr.domain.feed.di

import com.peekr.domain.feed.application.usecase.FeedUseCases
import com.peekr.domain.feed.application.usecase.GetFeedsUseCase
import com.peekr.domain.feed.domain.repository.FeedRepository
import com.peekr.domain.feed.infrastructure.repository.FeedRepositoryImpl
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { FeedRepositoryImpl() }

    // UseCases
    factory { GetFeedsUseCase(get()) }
    single<FeedUseCases> { FeedUseCases(get()) }
}
