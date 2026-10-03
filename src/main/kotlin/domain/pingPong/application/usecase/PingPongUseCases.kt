package com.turnin.domain.pingPong.application.usecase

data class PingPongUseCases(
    /** @see CreatePingPongUseCase */
    val create: CreatePingPongUseCase,
    /** @see CreatePingPongAnswerUseCase */
    val createAnswer: CreatePingPongAnswerUseCase,
    /** @see GetPingPongsUseCase */
    val getPingPongs: GetPingPongsUseCase,
)
