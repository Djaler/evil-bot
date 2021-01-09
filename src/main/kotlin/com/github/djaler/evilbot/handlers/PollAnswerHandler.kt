package com.github.djaler.evilbot.handlers

import dev.inmo.tgbotapi.types.UPDATE_POLL_ANSWER
import dev.inmo.tgbotapi.types.polls.PollAnswer
import dev.inmo.tgbotapi.types.update.PollAnswerUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update

abstract class PollAnswerHandler : UpdateHandler {
    override val updateType get() = UPDATE_POLL_ANSWER

    override suspend fun handleUpdate(update: Update): Boolean {
        if (update !is PollAnswerUpdate) {
            return false
        }

        handleAnswer(update.data)

        return true
    }

    abstract suspend fun handleAnswer(answer: PollAnswer)
}