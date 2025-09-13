package com.peekr.domain.userKeyword.application.usecase

data class UserKeywordUseCases(
    val get: GetUserKeywordsUseCase,
    val create: CreateUserKeywordUseCase,
    val update: UpdateUserKeywordUseCase,
    val delete: DeleteUserKeywordUseCase,
)
