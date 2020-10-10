package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.extensions.utils.formatting.link
import dev.inmo.tgbotapi.types.ParseMode.ParseMode

fun createStickerpackLink(stickerPackName: String, parseMode: ParseMode): String {
    return (stickerPackName to "https://t.me/addstickers/$stickerPackName").link(parseMode)
}
