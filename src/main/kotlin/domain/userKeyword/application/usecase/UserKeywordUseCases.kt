package com.peekr.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    /** @see GetUserKeywordsUseCase */
    val get: GetUserKeywordsUseCase,
    /** @see CreateUserKeywordUseCase */
    val create: CreateUserKeywordUseCase,
    /** @see UpdateOffsetUseCase */
    val updateOffset: UpdateOffsetUseCase,
    /** @see UpdateDescriptionUseCase */
    val updateDescription: UpdateDescriptionUseCase,
    /** @see DeleteUserKeywordUseCase */
    val delete: DeleteUserKeywordUseCase,
)
