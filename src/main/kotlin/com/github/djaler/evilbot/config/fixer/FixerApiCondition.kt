package com.github.djaler.evilbot.config.fixer

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class FixerApiCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.getProperty("fixer.api.key", "").isNotBlank()
    }
}
