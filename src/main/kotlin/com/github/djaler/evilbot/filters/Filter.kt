package com.github.djaler.evilbot.filters

import com.github.insanusmokrassar.TelegramBotAPI.types.message.abstracts.Message

interface Filter {
    suspend fun filter(message: Message): Boolean
}

infix fun Filter.and(other: Filter): Filter = AndFilter(this, other)

infix fun Filter.or(other: Filter): Filter = OrFilter(this, other)

operator fun Filter.not(): Filter = InvertedFilter(this)

private class AndFilter(private val firstFilter: Filter, private val secondFilter: Filter) : Filter {
    override suspend fun filter(message: Message) = firstFilter.filter(message) && secondFilter.filter(message)
}

private class OrFilter(private val firstFilter: Filter, private val secondFilter: Filter) : Filter {
    override suspend fun filter(message: Message) = firstFilter.filter(message) || secondFilter.filter(message)
}

private class InvertedFilter(private val originalFilter: Filter) : Filter {
    override suspend fun filter(message: Message) = !originalFilter.filter(message)
}
