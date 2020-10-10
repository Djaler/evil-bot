package com.github.djaler.evilbot.filters.message

import dev.inmo.tgbotapi.types.message.abstracts.Message

interface MessageFilter {
    suspend fun filter(message: Message): Boolean
}

infix fun MessageFilter.and(other: MessageFilter): MessageFilter =
    AndFilter(this, other)

infix fun MessageFilter.or(other: MessageFilter): MessageFilter =
    OrFilter(this, other)

operator fun MessageFilter.not(): MessageFilter =
    InvertedFilter(this)

private class AndFilter(private val firstFilter: MessageFilter, private val secondFilter: MessageFilter) :
    MessageFilter {
    override suspend fun filter(message: Message) = firstFilter.filter(message) && secondFilter.filter(message)
}

private class OrFilter(private val firstFilter: MessageFilter, private val secondFilter: MessageFilter) :
    MessageFilter {
    override suspend fun filter(message: Message) = firstFilter.filter(message) || secondFilter.filter(message)
}

private class InvertedFilter(private val originalFilter: MessageFilter) :
    MessageFilter {
    override suspend fun filter(message: Message) = !originalFilter.filter(message)
}
