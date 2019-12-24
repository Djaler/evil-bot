package com.github.djaler.evilbot.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate

@Configuration
class RestConfig {
    @Bean
    fun casRestTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .defaultHeader("User-Agent", "evil-bot")
            .messageConverters(MappingAnyJsonHttpMessageConverter())
            .build()
    }
}

private class MappingAnyJsonHttpMessageConverter : MappingJackson2HttpMessageConverter() {
    init {
        this.supportedMediaTypes = listOf(MediaType.ALL)
    }
}
