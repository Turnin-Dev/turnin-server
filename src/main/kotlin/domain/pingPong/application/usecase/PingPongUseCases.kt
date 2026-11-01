package com.turnin.domain.pingPong.application.usecase

data class PingPongUseCases(
    /** @see CreatePingPongUseCase */
    val createPingPong: CreatePingPongUseCase,
    /** @see CreatePingPongAnswerUseCase */
    val createAnswer: CreatePingPongAnswerUseCase,
    /** @see GetPingPongsUseCase */
    val getPingPongs: GetPingPongsUseCase,
    /** @see DeletePingPongUseCase */
    val deletePingPong: DeletePingPongUseCase,
    /** @see DeletePingPongAnswerUseCase */
    val deleteAnswer: DeletePingPongAnswerUseCase,
)
