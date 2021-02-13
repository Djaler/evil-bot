package com.github.djaler.evilbot.config

import com.github.djaler.evilbot.clients.SentryClient
import org.apache.logging.log4j.LogManager
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.cache.interceptor.SimpleCacheErrorHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration

@Configuration
class CacheConfig(private val sentryClient: SentryClient) : CachingConfigurerSupport() {
    companion object {
        private val log = LogManager.getLogger()
    }

    override fun errorHandler(): CacheErrorHandler {
        return object : SimpleCacheErrorHandler() {
            override fun handleCacheGetError(exception: RuntimeException, cache: Cache, key: Any) {
                try {
                    log.error("Cache get error", exception)
                    sentryClient.captureException(exception)

                    cache.evict(key)
                } catch (e: Exception) {
                    log.error("Cache evict error", e)
                    sentryClient.captureException(e)
                }
            }
        }
    }

    @Bean
    fun redisCacheManagerBuilderCustomizer(cacheProperties: CacheProperties) =
        RedisCacheManagerBuilderCustomizer { builder ->
            builder.withInitialCacheConfigurations(
                cacheProperties.durations.mapValues {
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(it.value)
                }
            )
        }
}
