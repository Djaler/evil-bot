package com.github.djaler.evilbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    ApiContextInitializer.init()

    runApplication<Application>(*args)
}
