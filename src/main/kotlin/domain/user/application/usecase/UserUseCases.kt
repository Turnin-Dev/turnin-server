package com.peekr.domain.user.application.usecase

data class UserUseCases(
    /** @see [GetUserUseCase] */
    val get: GetUserUseCase,
    /** @see [GetProfileUseCase] */
    val getProfile: GetProfileUseCase,
    /** @see [GetOtherUserProfileUseCase] */
    val getOtherUserProfile: GetOtherUserProfileUseCase,
    /** @see [UpdateUserUseCase] */
    val update: UpdateUserUseCase,
    /** @see [UpdateIntroduceUseCase] */
    val updateIntroduce: UpdateIntroduceUseCase,
)
