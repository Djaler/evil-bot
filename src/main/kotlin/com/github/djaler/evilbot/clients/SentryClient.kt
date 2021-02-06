package com.github.djaler.evilbot.clients

import io.sentry.IHub
import org.springframework.stereotype.Component

@Component
class SentryClient(private val hub: IHub) : IHub by hub