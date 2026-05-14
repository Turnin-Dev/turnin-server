package com.turnin.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    /** @see GetUserKeywordsUseCase */
    val get: GetUserKeywordsUseCase,
    /** @see GetDetailUseCase */
    val getDetail: GetDetailUseCase,
    /** @see GetDetailsUseCase */
    val getDetails: GetDetailsUseCase,
    /** @see CreateUserKeywordUseCase */
    val create: CreateUserKeywordUseCase,
    /** @see UpdateUserKeywordUseCase */
    val update: UpdateUserKeywordUseCase,
    /** @see DeleteUserKeywordUseCase */
    val delete: DeleteUserKeywordUseCase,
)
