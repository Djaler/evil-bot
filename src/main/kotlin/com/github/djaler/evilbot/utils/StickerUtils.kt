package com.github.djaler.evilbot.utils

import com.github.insanusmokrassar.TelegramBotAPI.types.ParseMode.ParseMode
import com.github.insanusmokrassar.TelegramBotAPI.utils.link

fun createStickerpackLink(stickerPackName: String, parseMode: ParseMode): String {
    return (stickerPackName to "https://t.me/addstickers/$stickerPackName").link(parseMode)
}
