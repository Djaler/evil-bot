package com.github.djaler.evilbot.handlers.base

import dev.inmo.tgbotapi.extensions.utils.asPollAnswerUpdate
import dev.inmo.tgbotapi.types.UPDATE_POLL_ANSWER
import dev.inmo.tgbotapi.types.polls.PollAnswer
import dev.inmo.tgbotapi.types.update.abstracts.Update

abstract class PollAnswerHandler : UpdateHandler {
    override val updateType get() = UPDATE_POLL_ANSWER

    override suspend fun handleUpdate(update: Update): Boolean {
        val answer = update.asPollAnswerUpdate()?.data ?: return false

        handleAnswer(answer)

        return true
    }

    abstract suspend fun handleAnswer(answer: PollAnswer)
}
