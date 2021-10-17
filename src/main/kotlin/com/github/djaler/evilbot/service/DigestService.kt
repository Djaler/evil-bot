package com.github.djaler.evilbot.service

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import java.io.InputStream

@Service
class DigestService {
    fun getInputStreamDigest(inputStream: InputStream): String {
        return DigestUtils.md5DigestAsHex(inputStream)
    }
}
