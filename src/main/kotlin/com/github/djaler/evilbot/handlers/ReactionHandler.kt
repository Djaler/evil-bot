package com.github.djaler.evilbot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.djaler.evilbot.components.TelegramClient
import com.github.djaler.evilbot.filters.Filters
import com.github.djaler.evilbot.filters.or
import com.github.djaler.evilbot.model.Reaction
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Message
import javax.annotation.PostConstruct
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.random.Random

@Component
class ReactionHandler(
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
    private val botUsername: String,
    private val telegramClient: TelegramClient
) : MessageHandler(filter = Filters.Text or Filters.Command) {
    private lateinit var reactions: List<Reaction>

    @PostConstruct
    fun init() {
        val resources = PathMatchingResourcePatternResolver().getResources("classpath:reactions/*.json")

        val loadedReactions = mutableListOf<Reaction>()

        for (resource in resources) {
            val reactions: List<Reaction> = objectMapper.readValue(resource.inputStream)
            for (reaction in reactions) {
                val violations = validator.validate(reaction)
                if (violations.isNotEmpty()) {
                    throw ConstraintViolationException(violations)
                }

                loadedReactions.add(reaction)
            }
        }

        reactions = loadedReactions
    }

    override fun handleMessage(message: Message): Boolean {
        val messageReplyToBot = message.isReply && message.replyToMessage.from.userName == botUsername

        for (reaction in reactions) {
            val chance = reaction.chance

            if (chance < Random.nextDouble(0.0, 100.0)) {
                continue
            }

            if (reaction.replyToBot && !messageReplyToBot) {
                continue
            }

            if (reaction.triggers.isNotEmpty() &&
                reaction.triggers.none { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(message.text) }
            ) {
                continue
            }

            telegramClient.replyTextTo(message, reaction.reactions.random())

            return true
        }

        return false
    }
}
