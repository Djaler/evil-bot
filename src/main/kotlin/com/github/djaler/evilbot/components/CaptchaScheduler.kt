package com.github.djaler.evilbot.components

import org.apache.logging.log4j.LogManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CaptchaScheduler {
    companion object {
        private val log = LogManager.getLogger()
    }

    @Scheduled(fixedDelay = 1000 * 30)
    fun test() {
        log.info("Scheduler run")
    }
}
