package com.peekr.domain.user.application.usecase

data class UserUseCases(
    /** @see [GetUserUseCase] */
    val get: GetUserUseCase,
    /** @see [GetMyProfileUseCase] */
    val getMyProfile: GetMyProfileUseCase,
    /** @see [GetUserProfileUseCase] */
    val getUserProfile: GetUserProfileUseCase,
    /** @see [UpdateUserUseCase] */
    val update: UpdateUserUseCase,
    /** @see [UpdateIntroduceUseCase] */
    val updateIntroduce: UpdateIntroduceUseCase,
)
