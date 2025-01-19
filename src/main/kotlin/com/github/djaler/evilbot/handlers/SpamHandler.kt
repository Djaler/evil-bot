package com.github.djaler.evilbot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.CallbackQueryHandler
import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.UserService
import com.github.djaler.evilbot.utils.createCallbackDataForHandler
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.chat.members.banChatMember
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asFromUserMessage
import dev.inmo.tgbotapi.extensions.utils.asPossiblyReplyMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct


@Component
class SpamHandler(
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val userService: UserService,
    private val requestsExecutor: RequestsExecutor
) : CommonMessageHandler() {

    private lateinit var triggers: List<String>

    override val order = Int.MIN_VALUE

    @PostConstruct
    fun init() {
        val resources = PathMatchingResourcePatternResolver().getResources("classpath:spam_trigger.json")

        triggers = resources.flatMap { resource ->
            val reactions: List<String> = objectMapper.readValue(resource.inputStream)
            reactions
        }
    }

    @OptIn(PreviewFeature::class)
    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        val content = message.content.asTextContent() ?: return false

        if (triggers.none { Regex(it, RegexOption.IGNORE_CASE).containsMatchIn(content.text) }
        ) {
            return false
        }

        val chat = message.chat.asPublicChat() ?: return false
        val user = message.asFromUserMessage()?.user ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val (userEntity, _) = userService.getOrCreateUserFrom(user)

        val statistic = userService.getStatistic(userEntity, chatEntity)

        if (statistic == null || statistic.messagesCount < 20) {
            val buttons = listOf(
                CallbackDataInlineKeyboardButton(
                    "Пощадить",
                    createCallbackDataForHandler(
                        SpamCallbackData(userId = user.id, banChatMember = false).encode(),
                        SpamCallbackHandler::class.java
                    )
                ),
                CallbackDataInlineKeyboardButton(
                    "Нет пощады",
                    createCallbackDataForHandler(
                        SpamCallbackData(userId = user.id, banChatMember = true).encode(),
                        SpamCallbackHandler::class.java
                    )
                )
            )

            requestsExecutor.reply(
                message,
                "Ты похож на скамера, что делать?",
                replyMarkup = InlineKeyboardMarkup(listOf(buttons))
            )

            return true
        }
        return false
    }
}

@Component
class SpamCallbackHandler(
    private val requestsExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
) : CallbackQueryHandler() {

    companion object {
        private val log = LogManager.getLogger()
    }

    @OptIn(PreviewFeature::class)
    override suspend fun handleCallback(query: MessageDataCallbackQuery, data: String) {
        val chat = query.message.chat
        val callbackData = SpamCallbackData.decode(data)

        if (query.user.id == callbackData.userId) {
            requestsExecutor.answerCallbackQuery(query, "А тебя не спрашивали")
            return
        }

        if (callbackData.banChatMember) {
            requestsExecutor.answerCallbackQuery(query, "Никакой пощады для скамеров")
            try {
                requestsExecutor.banChatMember(chat.id, callbackData.userId)
                query.message.asPossiblyReplyMessage()?.replyTo?.let {
                    requestsExecutor.deleteMessage(chat.id, it.messageId)
                }
            } catch (e: Exception) {
                log.error("Exception in scam ban: ", e)
                sentryClient.captureException(e)
                query.message.asPossiblyReplyMessage()?.replyTo?.let {
                    requestsExecutor.reply(it, "Этот скамер слишком хорош, чтобы быть забаненным")
                }
            }
        } else {
            requestsExecutor.answerCallbackQuery(query, "Ну ладно")
            query.message.asPossiblyReplyMessage()?.replyTo?.let {
                requestsExecutor.reply(it, "Сомнительно, но окей")
            }
        }
        requestsExecutor.deleteMessage(chat.id, query.message.messageId)
    }
}

data class SpamCallbackData(
    val userId: UserId,
    val banChatMember: Boolean
) {
    companion object {
        fun decode(data: String): SpamCallbackData {
            val value = data.split(":")
            return SpamCallbackData(
                UserId(value[0].toLong()),
                value[1].toBoolean()
            )
        }
    }

    fun encode(): String {
        return "${userId.chatId}:${banChatMember}"
    }
}
