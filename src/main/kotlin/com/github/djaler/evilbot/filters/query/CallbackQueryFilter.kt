package com.github.djaler.evilbot.filters.query

import dev.inmo.tgbotapi.types.queries.callback.MessageCallbackQuery

interface CallbackQueryFilter {
    suspend fun filter(query: MessageCallbackQuery): Boolean
}

infix fun CallbackQueryFilter.and(other: CallbackQueryFilter): CallbackQueryFilter =
    AndFilter(this, other)

infix fun CallbackQueryFilter.or(other: CallbackQueryFilter): CallbackQueryFilter =
    OrFilter(this, other)

operator fun CallbackQueryFilter.not(): CallbackQueryFilter =
    InvertedFilter(this)

private class AndFilter(private val firstFilter: CallbackQueryFilter, private val secondFilter: CallbackQueryFilter) :
    CallbackQueryFilter {
    override suspend fun filter(query: MessageCallbackQuery) = firstFilter.filter(query) && secondFilter.filter(query)
}

private class OrFilter(private val firstFilter: CallbackQueryFilter, private val secondFilter: CallbackQueryFilter) :
    CallbackQueryFilter {
    override suspend fun filter(query: MessageCallbackQuery) = firstFilter.filter(query) || secondFilter.filter(query)
}

private class InvertedFilter(private val originalFilter: CallbackQueryFilter) :
    CallbackQueryFilter {
    override suspend fun filter(query: MessageCallbackQuery) = !originalFilter.filter(query)
}
