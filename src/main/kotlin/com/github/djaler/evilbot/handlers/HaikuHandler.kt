package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.CommonMessageHandler
import com.github.djaler.evilbot.service.HaikuService
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.italic
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.springframework.stereotype.Component

@Component
class HaikuHandler(
    private val requestsExecutor: RequestsExecutor,
    private val haikuService: HaikuService
) : CommonMessageHandler() {

    override val order = 100

    @OptIn(PreviewFeature::class)
    override suspend fun handleMessage(message: CommonMessage<*>): Boolean {
        val text = message.content.asTextContent()?.text ?: return false

        val lines = haikuService.tryParseHaiku(text) ?: return false

        requestsExecutor.reply(
            message,
            buildEntities {
                +"🌸\n"
                italic(lines[0]) + "\n"
                italic(lines[1]) + "\n"
                italic(lines[2])
            }
        )

        return false
    }
}
