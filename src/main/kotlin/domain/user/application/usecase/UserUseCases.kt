package com.peekr.domain.user.application.usecase

data class UserUseCases(
    val get: GetUserUseCase,
    val getProfile: GetUserProfileUseCase,
    val update: UpdateUserUseCase,
)
