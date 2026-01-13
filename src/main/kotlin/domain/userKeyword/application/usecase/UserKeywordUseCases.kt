package com.peekr.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    /** @see GetUserKeywordsUseCase */
    val get: GetUserKeywordsUseCase,
    /** @see GetDetailUseCase */
    val getDetail: GetDetailUseCase,
    /** @see GetDetailsUseCase */
    val getDetails: GetDetailsUseCase,
    /** @see GetDescriptionUseCase */
    val getDescription: GetDescriptionUseCase,
    /** @see CreateUserKeywordUseCase */
    val create: CreateUserKeywordUseCase,
    /** @see UpdateDescriptionUseCase */
    val updateDescription: UpdateDescriptionUseCase,
    /** @see DeleteUserKeywordUseCase */
    val delete: DeleteUserKeywordUseCase,
)
