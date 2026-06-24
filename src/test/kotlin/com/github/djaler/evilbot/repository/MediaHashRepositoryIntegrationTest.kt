package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.MediaHash
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.awt.Color
import java.awt.image.BufferedImage
import java.time.Duration
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DuplicateMediaChecker::class)
@Testcontainers
class MediaHashRepositoryIntegrationTest {
    @Autowired
    private lateinit var repository: MediaHashRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var checker: DuplicateMediaChecker

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
    }

    private fun newChat(): Chat = entityManager.persistAndFlush(Chat(telegramId = 0, title = "test"))

    private fun mediaHash(chatId: Short, hash: Long, messageId: Long, lastSeenAt: Instant = Instant.now()) =
        MediaHash(hash = hash, chatId = chatId, messageId = messageId, fileId = "f", lastSeenAt = lastSeenAt)

    @Test
    fun `finds duplicate within hamming threshold and ignores beyond`() {
        val chatId = newChat().id
        repository.saveAndFlush(mediaHash(chatId, hash = 0L, messageId = 100))

        // distance 0 — same hash
        repository.findByChatIdAndHashCloseTo(chatId, 0L, 5).shouldNotBeNull().messageId shouldBe 100L
        // distance 5 — 0b11111, on the threshold
        repository.findByChatIdAndHashCloseTo(chatId, 0b11111L, 5).shouldNotBeNull()
        // distance 6 — 0b111111, beyond the threshold
        repository.findByChatIdAndHashCloseTo(chatId, 0b111111L, 5).shouldBeNull()
    }

    @Test
    fun `handles negative hashes with the high bit set`() {
        val chatId = newChat().id
        // Long.MIN_VALUE has only bit 63 set -> distance 1 from 0
        repository.saveAndFlush(mediaHash(chatId, hash = Long.MIN_VALUE, messageId = 200))

        repository.findByChatIdAndHashCloseTo(chatId, 0L, 5).shouldNotBeNull().messageId shouldBe 200L
    }

    @Test
    fun `checkAndRecord shifts the row forward on repost`() {
        val chat = newChat()
        val image = halfWhiteImage()

        // first sighting — new row, no previous
        checker.checkAndRecord(image, chat, MessageId(100L), FileId("f")).shouldBeNull()
        // repost — links to the previous message and moves the row forward
        checker.checkAndRecord(image, chat, MessageId(200L), FileId("f")) shouldBe 100L

        repository.count() shouldBe 1L
        repository.findByChatIdAndHashCloseTo(chat.id, 0L, 64).shouldNotBeNull().messageId shouldBe 200L
    }

    @Test
    fun `deleteByLastSeenAtBefore removes only stale rows`() {
        val chatId = newChat().id
        val now = Instant.now()
        repository.saveAndFlush(mediaHash(chatId, hash = 0L, messageId = 1, lastSeenAt = now))
        repository.saveAndFlush(mediaHash(chatId, hash = 1L, messageId = 2, lastSeenAt = now.minus(Duration.ofDays(200))))

        val deleted = repository.deleteByLastSeenAtBefore(now.minus(Duration.ofDays(180)))

        deleted shouldBe 1
        repository.count() shouldBe 1L
    }

    @Test
    fun `findVideoCandidates respects hash distance and duration window`() {
        val chatId = newChat().id
        repository.saveAndFlush(
            MediaHash(hash = 0L, chatId = chatId, messageId = 100, fileId = "f", lastSeenAt = Instant.now(), duration = 10L)
        )

        // same hash, window includes duration 10
        repository.findVideoCandidates(chatId, 0L, 5, 9L, 11L).size shouldBe 1

        // same hash, window excludes duration 10
        repository.findVideoCandidates(chatId, 0L, 5, 11L, 13L).size shouldBe 0

        // hash distance 6 (0b111111), maxDistance 5 — fails distance gate
        repository.findVideoCandidates(chatId, 0b111111L, 5, 9L, 11L).size shouldBe 0
    }

    @Test
    fun `frame_hashes BIGINT array round-trips through the database`() {
        val chatId = newChat().id

        // non-null frame hashes
        val saved = repository.saveAndFlush(
            MediaHash(hash = 0L, chatId = chatId, messageId = 300, fileId = "f", lastSeenAt = Instant.now(), frameHashes = listOf(1L, 2L, 3L, 4L))
        )
        entityManager.clear()
        repository.findById(saved.id).get().frameHashes shouldBe listOf(1L, 2L, 3L, 4L)

        // null frame hashes
        val savedNull = repository.saveAndFlush(
            MediaHash(hash = 1L, chatId = chatId, messageId = 301, fileId = "f", lastSeenAt = Instant.now(), frameHashes = null)
        )
        entityManager.clear()
        repository.findById(savedNull.id).get().frameHashes shouldBe null
    }

    private fun halfWhiteImage(): BufferedImage =
        BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB).apply {
            createGraphics().apply {
                color = Color.WHITE
                fillRect(0, 0, 10, 20)
                dispose()
            }
        }
}
