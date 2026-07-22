package com.turnin.domain.feed.application.usecase

data class FeedUseCases(
    /** @see GetAllFeedsUseCase */
    val allFeeds: GetAllFeedsUseCase,
    /** @see GetFriendFeedsUseCase */
    val friendFeeds: GetFriendFeedsUseCase,
)
