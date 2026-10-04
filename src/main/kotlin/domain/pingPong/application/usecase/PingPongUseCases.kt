package com.turnin.domain.pingPong.application.usecase

data class PingPongUseCases(
    /** @see CreatePingPongQuestionUseCase */
    val createQuestion: CreatePingPongQuestionUseCase,
    /** @see CreatePingPongAnswerUseCase */
    val createAnswer: CreatePingPongAnswerUseCase,
    /** @see GetPingPongsUseCase */
    val getPingPongs: GetPingPongsUseCase,
    /** @see DeletePingPongQuestionUseCase */
    val deleteQuestion: DeletePingPongQuestionUseCase,
    /** @see DeletePingPongAnswerUseCase */
    val deleteAnswer: DeletePingPongAnswerUseCase,
)
