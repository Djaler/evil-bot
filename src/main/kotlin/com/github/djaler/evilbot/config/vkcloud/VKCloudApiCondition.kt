package com.github.djaler.evilbot.config.vkcloud

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class VKCloudApiCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getProperty("vk.api.key", "").isNotBlank()
    }
}
