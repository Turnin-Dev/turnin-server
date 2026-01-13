package com.peekr.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    /** @see GetUserKeywordsUseCase */
    val get: GetUserKeywordsUseCase,
    /** @see GetUserKeywordDetailUseCase */
    val getDetail: GetUserKeywordDetailUseCase,
    /** @see GetDescriptionUseCase */
    val getDescription: GetDescriptionUseCase,
    /** @see CreateUserKeywordUseCase */
    val create: CreateUserKeywordUseCase,
    /** @see UpdateDescriptionUseCase */
    val updateDescription: UpdateDescriptionUseCase,
    /** @see DeleteUserKeywordUseCase */
    val delete: DeleteUserKeywordUseCase,
)
