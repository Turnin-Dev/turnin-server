package com.peekr.domain.friend.application.usecase

data class FriendUseCases(
    /** @see GetFriendsUseCase */
    val getFriends: GetFriendsUseCase,
    /** @see GetIncomingRequestsUseCase */
    val getIncomingRequesters: GetIncomingRequestsUseCase,
    /** @see AddFriendUseCase */
    val add: AddFriendUseCase,
    /** @see UpdateFriendRequestStatusUseCase */
    val updateStatus: UpdateFriendRequestStatusUseCase,
    /** @see DeleteFriendUseCase */
    val delete: DeleteFriendUseCase,
)
