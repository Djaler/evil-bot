package com.github.djaler.evilbot.utils

import dev.inmo.tgbotapi.utils.EntitiesBuilder
import dev.inmo.tgbotapi.utils.link

fun EntitiesBuilder.stickerpackLink(stickerPackName: String): EntitiesBuilder {
    return link(stickerPackName, createStickerpackUrl(stickerPackName))
}

fun createStickerpackUrl(stickerPackName: String): String {
    return "https://t.me/addstickers/$stickerPackName"
}
