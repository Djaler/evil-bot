package com.github.djaler.evilbot.utils

import com.github.insanusmokrassar.TelegramBotAPI.extensions.utils.formatting.link
import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.ParseMode

fun createStickerpackLink(stickerPackName: String, parseMode: ParseMode): String {
    return (stickerPackName to "https://t.me/addstickers/$stickerPackName").link(parseMode)
}
