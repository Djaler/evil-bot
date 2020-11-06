package com.github.djaler.evilbot.config

import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.SimpleCacheErrorHandler
import org.springframework.context.annotation.Configuration

@Configuration
class CacheConfig : CachingConfigurerSupport() {
    override fun errorHandler(): CacheErrorHandler {
        return object : SimpleCacheErrorHandler() {
            override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
                cache.evict(key)
            }
        }
    }
}
