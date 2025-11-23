package com.peekr.domain.user.application.usecase

data class UserUseCases(
    val get: GetUserUseCase,
    val getProfile: GetProfileUseCase,
    val getOtherUserProfile: GetOtherUserProfileUseCase,
    val update: UpdateUserUseCase,
    val updateIntroduce: UpdateIntroduceUseCase,
)
