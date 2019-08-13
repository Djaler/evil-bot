package com.github.djaler.evilbot.components

import com.github.djaler.evilbot.entity.User
import org.springframework.stereotype.Component

@Component
class TelegramLinksHelper {
    companion object {
        private const val MARKDOWN_ESCAPE_CHARS = """\*_`\["""
    }

    fun createStickerpackLink(stickerPackName: String): String {
        return "[$stickerPackName](https://t.me/addstickers/$stickerPackName)"
    }

    fun createUserMentionLink(user: User): String {
        return "[${escapeMarkdown(user.username)}](tg://user?id=${user.telegramId})"
    }

    private fun escapeMarkdown(text: String): String {
        return Regex("([$MARKDOWN_ESCAPE_CHARS])").replace(text) { "//${it.value}" }
    }
}
