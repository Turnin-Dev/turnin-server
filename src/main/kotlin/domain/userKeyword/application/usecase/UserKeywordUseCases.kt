package com.peekr.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    /** @see GetUserKeywordsUseCase */
    val get: GetUserKeywordsUseCase,
    /** @see CreateUserKeywordUseCase */
    val create: CreateUserKeywordUseCase,
    /** @see UpdateUserKeywordUseCase */
    val update: UpdateUserKeywordUseCase,
    /** @see DeleteUserKeywordUseCase */
    val delete: DeleteUserKeywordUseCase,
)
