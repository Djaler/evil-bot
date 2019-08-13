package com.github.djaler.evilbot.utils

import java.security.MessageDigest

private val MD5Instance = MessageDigest.getInstance("MD5")

fun String.getMD5(): String {
    val digest = MD5Instance.digest(this.toByteArray())

    return digest.toHexString()
}

private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
