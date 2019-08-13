package com.github.djaler.evilbot.filters

import org.telegram.telegrambots.meta.api.objects.Message

interface Filter {
    fun filter(message: Message): Boolean
}

infix fun Filter.and(other: Filter): Filter = AndFilter(this, other)

infix fun Filter.or(other: Filter): Filter = OrFilter(this, other)

operator fun Filter.not(): Filter = InvertedFilter(this)

private class AndFilter(private val firstFilter: Filter, private val secondFilter: Filter) : Filter {
    override fun filter(message: Message) = firstFilter.filter(message) && secondFilter.filter(message)
}

private class OrFilter(private val firstFilter: Filter, private val secondFilter: Filter) : Filter {
    override fun filter(message: Message) = firstFilter.filter(message) || secondFilter.filter(message)
}

private class InvertedFilter(private val originalFilter: Filter) : Filter {
    override fun filter(message: Message) = !originalFilter.filter(message)
}
