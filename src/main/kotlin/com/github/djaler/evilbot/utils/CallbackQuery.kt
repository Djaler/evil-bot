package com.github.djaler.evilbot.utils

import com.github.djaler.evilbot.handlers.CallbackQueryHandler

fun <T : CallbackQueryHandler> createCallbackDataForHandler(callbackData: String, handlerClass: Class<T>): String {
    return encodeHandlerClass(handlerClass) + callbackData
}

fun <T : CallbackQueryHandler> isCallbackForHandler(callbackData: String, handlerClass: Class<T>): Boolean {
    val digest = encodeHandlerClass(handlerClass)

    return callbackData.startsWith(digest)
}

fun <T : CallbackQueryHandler> decodeCallbackData(callbackData: String, handlerClass: Class<T>): String {
    val digest = encodeHandlerClass(handlerClass)

    return callbackData.removePrefix(digest)
}

private fun <T : CallbackQueryHandler> encodeHandlerClass(handlerClass: Class<T>): String {
    return handlerClass.canonicalName.getMD5()
}
