package com.peekr.domain.feed.di

import com.peekr.domain.feed.domain.repository.FeedRepository
import com.peekr.domain.feed.infrastructure.repository.FeedRepositoryImpl
import org.koin.dsl.module

val feedModule = module {
    single<FeedRepository> { FeedRepositoryImpl() }
}
