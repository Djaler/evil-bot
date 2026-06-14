package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.MediaHash
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Testcontainers
class MediaHashRepositoryIntegrationTest {
    @Autowired
    private lateinit var repository: MediaHashRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
    }

    private fun newChatId(): Short =
        entityManager.persistAndFlush(Chat(telegramId = 0, title = "test")).id

    @Test
    fun `finds duplicate within hamming threshold and ignores beyond`() {
        val chatId = newChatId()
        repository.saveAndFlush(MediaHash(hash = 0L, chatId = chatId, messageId = 100, fileId = "f"))

        // distance 0 — same hash
        repository.findByChatIdAndHashCloseTo(chatId, 0L, 5).shouldNotBeNull().messageId shouldBe 100L
        // distance 5 — 0b11111, on the threshold
        repository.findByChatIdAndHashCloseTo(chatId, 0b11111L, 5).shouldNotBeNull()
        // distance 6 — 0b111111, beyond the threshold
        repository.findByChatIdAndHashCloseTo(chatId, 0b111111L, 5).shouldBeNull()
    }

    @Test
    fun `handles negative hashes with the high bit set`() {
        val chatId = newChatId()
        // Long.MIN_VALUE has only bit 63 set -> distance 1 from 0
        repository.saveAndFlush(MediaHash(hash = Long.MIN_VALUE, chatId = chatId, messageId = 200, fileId = "f"))

        repository.findByChatIdAndHashCloseTo(chatId, 0L, 5).shouldNotBeNull().messageId shouldBe 200L
    }
}
