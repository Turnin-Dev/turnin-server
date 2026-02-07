package com.peekr.domain.friend.application.usecase

data class FriendUseCases(
    /** @see GetFriendsPaginationUseCase */
    val getFriendsPagination: GetFriendsPaginationUseCase,
    /** @see GetIncomingRequestersUseCase */
    val getIncomingRequesters: GetIncomingRequestersUseCase,
    /** @see AddFriendUseCase */
    val add: AddFriendUseCase,
    /** @see UpdateFriendRequestStatusUseCase */
    val updateStatus: UpdateFriendRequestStatusUseCase,
    /** @see DeleteFriendUseCase */
    val delete: DeleteFriendUseCase,
)
