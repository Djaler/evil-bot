package com.github.djaler.evilbot.filters

import org.telegram.telegrambots.meta.api.objects.Message

object Filters {
    object Text : Filter {
        override fun filter(message: Message): Boolean {
            return message.hasText() && !message.isCommand
        }
    }

    object Command : Filter {
        override fun filter(message: Message) = message.isCommand
    }

    object Sticker : Filter {
        override fun filter(message: Message) = message.hasSticker()
    }

    object ReplyToSticker : Filter {
        override fun filter(message: Message) = message.isReply && message.replyToMessage.hasSticker()
    }

    object PrivateChat : Filter {
        override fun filter(message: Message): Boolean = message.chat.isUserChat
    }

    object NewChatMember : Filter {
        override fun filter(message: Message): Boolean = !message.newChatMembers.isNullOrEmpty()
    }
}


