package com.github.djaler.evilbot.utils

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

inline fun <reified T> cached(cache: Cache, key: Any, block: () -> T): T {
    val cached = cache.get(key, T::class.java)
    if (cached != null) {
        return cached
    }

    return block().also {
        cache.put(key, it)
    }
}

fun CacheManager.getCacheOrThrow(cacheName: String): Cache {
    return getCache(cacheName) ?: throw IllegalArgumentException("Cache $cacheName not found")
}
