package com.github.djaler.evilbot.config.locationiq

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class LocationiqApiCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getProperty("locationiq.api.key", "").isNotBlank()
    }
}
