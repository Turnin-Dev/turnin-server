package com.turnin.domain.keyword.application.usecase

data class KeywordUseCases(
    val get: GetKeywordUseCase,
    val getByName: GetKeywordByNameUseCase,
    val create: CreateKeywordUseCase,
)
