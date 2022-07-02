package com.github.djaler.evilbot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.djaler.evilbot.components.TelegramMediaSender
import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.model.Reaction
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.replyWithSticker
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.chat.ExtendedBot
import dev.inmo.tgbotapi.types.message.HTMLParseMode
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import kotlin.random.Random

@Component
class ReactionHandler(
    private val objectMapper: ObjectMapper,
    private val validator: Validator,
    private val botInfo: ExtendedBot,
    private val requestsExecutor: RequestsExecutor,
    private val telegramMediaSender: TelegramMediaSender
) : CommonMessageHandler() {
    companion object {
        private const val STICKER_REACTION_PREFIX = "sticker:"
        private const val HTML_REACTION_PREFIX = "html:"
        private const val MEDIA_REACTION_PREFIX = "media:"
        private const val PHOTO_REACTION_PREFIX = "photo:"
    }

    private lateinit var reactions: List<Reaction>

    override val order = Int.MAX_VALUE

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

    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        val content = message.content.asTextContent() ?: return false

        val messageReplyToBot = isReplyToBot(message)

        for (reaction in reactions) {
            val chance = reaction.chance

            if (chance < Random.nextDouble(0.0, 100.0)) {
                continue
            }

            if (reaction.replyToBot && !messageReplyToBot) {
                continue
            }

            if (reaction.triggers.isNotEmpty() &&
                reaction.triggers.none { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(content.text) }
            ) {
                continue
            }

            react(message, reaction.reactions.random())

            return true
        }

        return false
    }

    private suspend fun react(message: Message, reaction: String) {
        when {
            reaction.startsWith(STICKER_REACTION_PREFIX) ->
                requestsExecutor.replyWithSticker(message, FileId(reaction.removePrefix(STICKER_REACTION_PREFIX)))
            reaction.startsWith(HTML_REACTION_PREFIX) ->
                requestsExecutor.reply(message, reaction.removePrefix(HTML_REACTION_PREFIX), parseMode = HTMLParseMode)
            reaction.startsWith(MEDIA_REACTION_PREFIX) ->
                telegramMediaSender.sendAnimation(
                    message.chat.id,
                    ClassPathResource((reaction.removePrefix(MEDIA_REACTION_PREFIX))),
                    replyTo = message.messageId
                )
            reaction.startsWith(PHOTO_REACTION_PREFIX) ->
                telegramMediaSender.sendPhoto(
                    message.chat.id,
                    ClassPathResource((reaction.removePrefix(PHOTO_REACTION_PREFIX))),
                    replyTo = message.messageId)
            else -> requestsExecutor.reply(message, reaction)
        }
    }

    private fun isReplyToBot(message: CommonMessage<*>): Boolean {
        val replyTo = message.replyTo

        if (replyTo !is FromUserMessage) {
            return false
        }

        return replyTo.user.username == botInfo.username
    }
}
