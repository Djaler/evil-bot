package com.github.djaler.evilbot.components

import org.springframework.stereotype.Component

@Component
class TelegramLinksHelper {
    fun createStickerpackLink(stickerPackName: String): String {
        return "[$stickerPackName](https://t.me/addstickers/$stickerPackName)"
    }
}
