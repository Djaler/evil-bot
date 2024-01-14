package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.VideoConvertService
import com.github.djaler.evilbot.utils.toTemporaryFile
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.media.sendVideo
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.withUploadVideoAction
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asDocumentContent
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.types.message.abstracts.Message
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import kotlin.io.path.createTempDirectory

@Component
class VideoConverterHandler(
    private val requestExecutor: RequestsExecutor,
    private val videoConvertService: VideoConvertService,
) : MessageHandler() {
    override suspend fun handleMessage(message: Message): Boolean {
        val document = message.asContentMessage()?.content?.asDocumentContent() ?: return false
        val mimeType = document.media.mimeType ?: return false
        if (mimeType.primaryType != "video") {
            return false
        }
        if (mimeType.subType == "mp4") {
            return false
        }

        requestExecutor.withUploadVideoAction(message.chat) {
            val videoBytes = requestExecutor.downloadFile(document)

            val mp4File = convertToMp4(videoBytes, document.media.fileName ?: "video")

            try {
                requestExecutor.sendVideo(message.chat, video = mp4File.asMultipartFile())
                requestExecutor.reply(message, text = "Вот тебе mp4, не благодари")
            } finally {
                mp4File.delete()
            }
        }

        return true
    }

    private fun convertToMp4(bytes: ByteArray, fileName: String): File {
        val directory = createTempDirectory(UUID.randomUUID().toString()).toFile()

        bytes.toTemporaryFile(directory, fileName).use { inputFile ->
            val outputFile = File(directory, inputFile.nameWithoutExtension + ".mp4")
            videoConvertService.convertToMp4(inputFile, outputFile)
            return outputFile
        }
    }
}
