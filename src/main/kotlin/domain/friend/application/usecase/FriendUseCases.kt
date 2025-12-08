package com.peekr.domain.friend.application.usecase

data class FriendUseCases(
    /** @see GetFriendsUseCase */
    val getFriends: GetFriendsUseCase,
    /** @see AddFriendUseCase */
    val add: AddFriendUseCase,
    /** @see UpdateFriendRequestStatusUseCase */
    val updateStatus: UpdateFriendRequestStatusUseCase,
    /** @see DeleteFriendUseCase */
    val delete: DeleteFriendUseCase,
)
