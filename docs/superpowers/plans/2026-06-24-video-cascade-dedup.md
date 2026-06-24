# Cascade Video Duplicate Detection — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop false "уже было" on videos that share a (often black) thumbnail but differ in content, by comparing duration and ffmpeg-extracted keyframes — but only when cheap metadata signals already collide.

**Architecture:** Two-tier cascade in `SeenMemeHandler`'s video branch. Tier 0 (metadata, no download): thumbnail dHash + `duration` gate → most non-duplicates rejected for free. Tier 1 (only on Tier-0 collision): download the video, extract 4 keyframes via ffmpeg, dHash each, compare vectors; lazily cache a candidate's frame vector in the DB. Photos are untouched.

**Tech Stack:** Kotlin, Spring Boot 3.4.5 (Hibernate 6.6), PostgreSQL 15, Flyway, `dev.inmo:tgbotapi` 33.1.0, ffmpeg via `ProcessBuilder`, JUnit 5 + Kotest assertions.

## Global Constraints

- Scope is **video only** (`VideoContent`). Do not touch the photo path. Do not re-add GIF/`AnimationContent` (removed in #235).
- No new third-party dependencies. ffmpeg is already in the Docker image and PATH.
- Bot API `getFile` cannot download files > 20 MB → `MAX_DOWNLOAD_BYTES = 20 * 1024 * 1024`.
- When Tier 1 cannot run (file too big / download / ffmpeg / decode failure) → **stay silent** (treat as not-a-duplicate). Zero false positives is the priority.
- Hamming threshold stays `5` (reuse existing `MAX_HAMMING_DISTANCE`). Duration tolerance `±1 s`.
- Heavy work (download + ffmpeg) must run **outside** any DB transaction; DB ops are short separate transactions.
- This project has no Spring/DB tests by deliberate choice. Unit-test only pure functions (à la `DHashTest`); verify persistence/ffmpeg/handler by build + manual run, as the existing `DuplicateMediaChecker`/`VideoConvertService` already do.
- `JAVA_HOME` must point at Java 17 before any Gradle run (see `jvm-gradle-build-issues`).

---

### Task 1: Pure keyframe helpers (TDD)

Three pure functions: hamming distance, frame-vector match (position-wise, strict majority), and frame-position selection. These hold the risky correctness and are fully unit-testable without Spring.

**Files:**
- Create: `src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt`
- Test: `src/test/kotlin/com/github/djaler/evilbot/service/VideoFingerprintTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces:
  - `internal const val FRAME_MAX_HAMMING_DISTANCE: Int` (= 5)
  - `internal fun hammingDistance(a: Long, b: Long): Int`
  - `internal fun framesMatch(a: List<Long>, b: List<Long>, maxDistance: Int = FRAME_MAX_HAMMING_DISTANCE): Boolean`
  - `internal fun framePositionsSeconds(durationSeconds: Long?, fractions: List<Double>, fallback: List<Double>): List<Double>`

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/com/github/djaler/evilbot/service/VideoFingerprintTest.kt`:

```kotlin
package com.github.djaler.evilbot.service

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VideoFingerprintTest {

    @Test
    fun `identical vectors match`() {
        framesMatch(listOf(1L, 2L, 3L, 4L), listOf(1L, 2L, 3L, 4L), maxDistance = 5) shouldBe true
    }

    @Test
    fun `one differing frame of four still matches`() {
        // last frame differs by 8 bits (> 5); 3 of 4 within threshold -> majority -> match
        framesMatch(listOf(1L, 2L, 3L, 0xFFL), listOf(1L, 2L, 3L, 0x00L), maxDistance = 5) shouldBe true
    }

    @Test
    fun `two differing frames of four do not match`() {
        // frames 3 and 4 differ by 8 bits each; only 2 of 4 within threshold -> no majority
        framesMatch(listOf(1L, 2L, 0xFFL, 0xFFL), listOf(1L, 2L, 0x00L, 0x00L), maxDistance = 5) shouldBe false
    }

    @Test
    fun `different lengths compare on the shorter overlap`() {
        framesMatch(listOf(1L, 2L), listOf(1L, 2L, 3L, 4L), maxDistance = 5) shouldBe true
    }

    @Test
    fun `empty overlap never matches`() {
        framesMatch(emptyList(), listOf(1L, 2L), maxDistance = 5) shouldBe false
    }

    @Test
    fun `positions are fractions of known duration`() {
        framePositionsSeconds(
            durationSeconds = 10L,
            fractions = listOf(0.2, 0.4, 0.6, 0.8),
            fallback = listOf(1.0, 2.0, 3.0, 4.0)
        ) shouldBe listOf(2.0, 4.0, 6.0, 8.0)
    }

    @Test
    fun `positions fall back when duration is null`() {
        framePositionsSeconds(
            durationSeconds = null,
            fractions = listOf(0.2, 0.4, 0.6, 0.8),
            fallback = listOf(1.0, 2.0, 3.0, 4.0)
        ) shouldBe listOf(1.0, 2.0, 3.0, 4.0)
    }

    @Test
    fun `positions fall back when duration is zero`() {
        framePositionsSeconds(
            durationSeconds = 0L,
            fractions = listOf(0.2, 0.4),
            fallback = listOf(1.0, 2.0)
        ) shouldBe listOf(1.0, 2.0)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests 'com.github.djaler.evilbot.service.VideoFingerprintTest'`
Expected: compilation failure / unresolved reference `framesMatch`, `framePositionsSeconds`.

- [ ] **Step 3: Write minimal implementation**

Create `src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt`:

```kotlin
package com.github.djaler.evilbot.service

import kotlin.math.roundToInt

internal const val FRAME_MAX_HAMMING_DISTANCE = 5

internal fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

/**
 * Покадровое (по индексу) сравнение векторов dHash на общем префиксе.
 * Дубликат, если совпало строгое большинство кадров (для 4 кадров — ≥ 3).
 */
internal fun framesMatch(a: List<Long>, b: List<Long>, maxDistance: Int = FRAME_MAX_HAMMING_DISTANCE): Boolean {
    val n = minOf(a.size, b.size)
    if (n == 0) return false
    val matching = (0 until n).count { hammingDistance(a[it], b[it]) <= maxDistance }
    return matching * 2 > n
}

/**
 * Относительные позиции кадров в секундах. При известной длительности — доли от неё,
 * иначе фиксированный фолбэк (лишние кадры просто не извлекутся ffmpeg-ом).
 */
internal fun framePositionsSeconds(
    durationSeconds: Long?,
    fractions: List<Double>,
    fallback: List<Double>
): List<Double> =
    if (durationSeconds != null && durationSeconds > 0) {
        fractions.map { (it * durationSeconds * 1000).roundToInt() / 1000.0 }
    } else {
        fallback
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests 'com.github.djaler.evilbot.service.VideoFingerprintTest'`
Expected: PASS (7 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt \
        src/test/kotlin/com/github/djaler/evilbot/service/VideoFingerprintTest.kt
git commit -m "feat: add pure keyframe comparison helpers for video dedup"
```

---

### Task 2: Persistence layer — migration, entity columns, candidate query

Add `duration` and `frame_hashes` columns and the duration-gated candidate finder. No unit tests (project convention); verify by build + a migration sanity check.

**Files:**
- Create: `src/main/resources/db/migration/V12__add_media_hashes_video_columns.sql`
- Modify: `src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt`
- Modify: `src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt`

**Interfaces:**
- Consumes: nothing from earlier tasks.
- Produces:
  - `MediaHash(hash: Long, chatId: Short, messageId: Long, fileId: String, lastSeenAt: Instant, duration: Long? = null, frameHashes: List<Long>? = null, id: Long = 0)` — new trailing params keep the existing 5-arg positional call valid.
  - `MediaHashRepository.findVideoCandidates(chatId: Short, hash: Long, maxDistance: Int, minDuration: Long, maxDuration: Long): List<MediaHash>`

- [ ] **Step 1: Write the migration**

Create `src/main/resources/db/migration/V12__add_media_hashes_video_columns.sql`:

```sql
ALTER TABLE media_hashes ADD COLUMN duration BIGINT;
ALTER TABLE media_hashes ADD COLUMN frame_hashes BIGINT[];
```

- [ ] **Step 2: Add entity columns**

In `src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt`, add imports and the two fields **before** `id` (so the existing 5-arg positional constructor call in the photo path keeps working):

```kotlin
package com.github.djaler.evilbot.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "media_hashes")
data class MediaHash(
    @Column
    val hash: Long,

    @Column
    val chatId: Short,

    @Column
    val messageId: Long,

    @Column
    val fileId: String,

    @Column
    val lastSeenAt: Instant,

    @Column
    val duration: Long? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "frame_hashes", columnDefinition = "bigint[]")
    val frameHashes: List<Long>? = null,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
```

- [ ] **Step 3: Add the duration-gated finder**

In `src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt`, add the method (keep the existing `findByChatIdAndHashCloseTo` and `deleteByLastSeenAtBefore`):

```kotlin
    /**
     * Tier 0 для видео: близкий хеш превью И длительность в пределах допуска.
     * Возвращает всех кандидатов (Tier 1 сверит контент по каждому). '#' — XOR, bit_count — число единичных битов.
     */
    @Query(
        value = "SELECT * FROM media_hashes WHERE chat_id = :chatId AND bit_count((hash # :hash)::bit(64)) <= :maxDistance AND duration BETWEEN :minDuration AND :maxDuration ORDER BY id",
        nativeQuery = true
    )
    fun findVideoCandidates(
        @Param("chatId") chatId: Short,
        @Param("hash") hash: Long,
        @Param("maxDistance") maxDistance: Int,
        @Param("minDuration") minDuration: Long,
        @Param("maxDuration") maxDuration: Long
    ): List<MediaHash>
```

- [ ] **Step 4: Build to verify it compiles**

Run: `./gradlew compileKotlin compileTestKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/migration/V12__add_media_hashes_video_columns.sql \
        src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt \
        src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt
git commit -m "feat: add duration and frame-hash columns for video dedup"
```

---

### Task 3: `VideoFingerprintService` — download + ffmpeg + dHash

Downloads a video (respecting the 20 MB limit), extracts keyframes via ffmpeg at the chosen positions, dHashes each, and returns the vector (or `null` when the deep check is impossible). No unit tests (needs ffmpeg + Telegram); verify by build + manual run.

**Files:**
- Modify: `src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt` (add the `@Service` class beneath the existing helpers — same file, mirroring how `DuplicateMediaChecker` lives next to `dHash`)

**Interfaces:**
- Consumes: `dHash(BufferedImage): Long` (existing, in `DuplicateMediaChecker.kt`), `framePositionsSeconds(...)` (Task 1), `downloadFile(fileId: FileId): ByteArray` (tgbotapi).
- Produces: `suspend fun VideoFingerprintService.extractFrameHashes(fileId: FileId, fileSize: Long?, durationSeconds: Long?): List<Long>?`

- [ ] **Step 1: Add the service**

Append to `src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt`:

```kotlin
import com.github.djaler.evilbot.clients.SentryClient
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.requests.abstracts.FileId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import javax.imageio.ImageIO

@Service
class VideoFingerprintService(
    private val requestExecutor: RequestsExecutor,
    private val sentryClient: SentryClient,
) {
    companion object {
        private val log = LogManager.getLogger()
        private const val MAX_DOWNLOAD_BYTES = 20L * 1024 * 1024
        private val FRAME_FRACTIONS = listOf(0.2, 0.4, 0.6, 0.8)
        private val FALLBACK_SECONDS = listOf(1.0, 2.0, 3.0, 4.0)
        private const val MIN_FRAMES = 2
    }

    /**
     * Вектор dHash ключевых кадров видео. null — если глубокую проверку выполнить нельзя
     * (файл > 20 МБ, ошибка скачивания/ffmpeg/декодирования или извлеклось < 2 кадров):
     * вызывающий трактует null как «проверка невозможна» и молчит.
     */
    suspend fun extractFrameHashes(fileId: FileId, fileSize: Long?, durationSeconds: Long?): List<Long>? {
        if (fileSize != null && fileSize > MAX_DOWNLOAD_BYTES) {
            return null
        }
        val positions = framePositionsSeconds(durationSeconds, FRAME_FRACTIONS, FALLBACK_SECONDS)
        return try {
            val bytes = requestExecutor.downloadFile(fileId)
            withContext(Dispatchers.IO) {
                val input = Files.createTempFile("dedup-", ".mp4").toFile()
                try {
                    input.writeBytes(bytes)
                    positions.mapNotNull { second -> extractFrameHash(input, second) }
                        .takeIf { it.size >= MIN_FRAMES }
                } finally {
                    input.delete()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to extract video frames, skipping deep duplicate check", e)
            sentryClient.captureException(e)
            null
        }
    }

    private fun extractFrameHash(input: File, second: Double): Long? {
        val output = Files.createTempFile("dedup-frame-", ".png").toFile()
        return try {
            val command = listOf(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "error",
                "-y",
                "-ss", second.toString(),
                "-i", input.absolutePath,
                "-frames:v", "1",
                output.absolutePath
            )
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            if (process.waitFor() != 0 || output.length() == 0L) {
                return null
            }
            val image = ImageIO.read(output) ?: return null
            dHash(image)
        } finally {
            output.delete()
        }
    }
}
```

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL. (If `RequestsExecutor` is not the correct bean type, mirror the injection already used in `SeenMemeHandler` — it injects `RequestsExecutor` and calls `downloadFile`.)

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/service/VideoFingerprint.kt
git commit -m "feat: add VideoFingerprintService extracting keyframe hashes via ffmpeg"
```

---

### Task 4: `DuplicateMediaChecker` — video DB operations

Add short transactional DB methods for the video cascade. Leave the existing photo `checkAndRecord` and `deleteOlderThan` untouched. No unit tests; verify by build.

**Files:**
- Modify: `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt`

**Interfaces:**
- Consumes: `MediaHashRepository.findVideoCandidates(...)` and `findByChatIdAndHashCloseTo(...)` (Task 2), `MediaHash(...)` constructor (Task 2).
- Produces (all on `DuplicateMediaChecker`):
  - `fun findVideoCandidates(chat: Chat, thumbHash: Long, duration: Long?): List<MediaHash>`
  - `fun recordVideo(thumbHash: Long, chat: Chat, messageId: MessageId, fileId: FileId, duration: Long?, frameHashes: List<Long>?)`
  - `fun shiftToCurrent(existing: MediaHash, messageId: MessageId): Long`
  - `fun cacheFrameHashes(existing: MediaHash, frameHashes: List<Long>)`

- [ ] **Step 1: Add the methods**

In `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt`, add imports `dev.inmo.tgbotapi.requests.abstracts.FileId` and `org.springframework.transaction.annotation.Transactional` (already imported), and add inside the class. Add `private const val DURATION_TOLERANCE_SECONDS = 1L` to the companion object next to `MAX_HAMMING_DISTANCE`:

```kotlin
    /**
     * Tier 0 для видео. С известной длительностью — гейт по hash + duration; без неё — только по hash
     * (эскалацию решает Tier 1).
     */
    @Transactional(readOnly = true)
    fun findVideoCandidates(chat: Chat, thumbHash: Long, duration: Long?): List<MediaHash> {
        if (duration != null) {
            return mediaHashRepository.findVideoCandidates(
                chat.id,
                thumbHash,
                MAX_HAMMING_DISTANCE,
                duration - DURATION_TOLERANCE_SECONDS,
                duration + DURATION_TOLERANCE_SECONDS
            )
        }
        return mediaHashRepository.findByChatIdAndHashCloseTo(chat.id, thumbHash, MAX_HAMMING_DISTANCE)
            ?.let(::listOf)
            ?: emptyList()
    }

    @Transactional
    fun recordVideo(
        thumbHash: Long,
        chat: Chat,
        messageId: MessageId,
        fileId: FileId,
        duration: Long?,
        frameHashes: List<Long>?
    ) {
        mediaHashRepository.save(
            MediaHash(thumbHash, chat.id, messageId.long, fileId.fileId, Instant.now(), duration, frameHashes)
        )
    }

    /** Сдвигает запись на текущее сообщение (продлевает TTL), возвращает предыдущий messageId. */
    @Transactional
    fun shiftToCurrent(existing: MediaHash, messageId: MessageId): Long {
        val previousMessageId = existing.messageId
        mediaHashRepository.save(existing.copy(messageId = messageId.long, lastSeenAt = Instant.now()))
        return previousMessageId
    }

    /** Ленивый кэш: до-сохраняет вектор кадров кандидату. */
    @Transactional
    fun cacheFrameHashes(existing: MediaHash, frameHashes: List<Long>) {
        mediaHashRepository.save(existing.copy(frameHashes = frameHashes))
    }
```

- [ ] **Step 2: Build to verify it compiles**

Run: `./gradlew compileKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt
git commit -m "feat: add video DB operations to DuplicateMediaChecker"
```

---

### Task 5: Wire the cascade into `SeenMemeHandler`

Split the handler: photos keep `checkAndRecord`; videos run the Tier 0 → Tier 1 cascade. Verify by build, then manual end-to-end run.

**Files:**
- Modify: `src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt`

**Interfaces:**
- Consumes: `dHash(image)` (existing in `DuplicateMediaChecker.kt`, `internal`, same module), `framesMatch(a, b)` and `VideoFingerprintService.extractFrameHashes(...)` (Tasks 1, 3), `DuplicateMediaChecker.findVideoCandidates/recordVideo/shiftToCurrent/cacheFrameHashes` (Task 4), `checkAndRecord` (existing).
- Produces: nothing downstream.

- [ ] **Step 1: Replace the handler body**

Rewrite `src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt`. Add constructor dependency `videoFingerprintService: VideoFingerprintService`, import `dHash`, `framesMatch`, `FileId`, and `VideoContent`/`PhotoContent` (already imported):

```kotlin
package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.clients.SentryClient
import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import com.github.djaler.evilbot.service.VideoFingerprintService
import com.github.djaler.evilbot.service.dHash
import com.github.djaler.evilbot.service.framesMatch
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import kotlinx.coroutines.CancellationException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class SeenMemeHandler(
    private val requestExecutor: RequestsExecutor,
    private val duplicateMediaChecker: DuplicateMediaChecker,
    private val videoFingerprintService: VideoFingerprintService,
    private val chatService: ChatService,
    private val sentryClient: SentryClient,
) : MessageHandler() {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleMessage(message: AccessibleMessage): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val content = message.asContentMessage()?.content ?: return false

        return when (content) {
            is PhotoContent -> {
                // для хеша 9x8 хватает самого маленького превью
                val preview = content.mediaCollection.minByOrNull { it.resolution } ?: return false
                val image = downloadImage(preview.fileId) ?: return false
                val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
                val previousMessageId =
                    duplicateMediaChecker.checkAndRecord(image, chatEntity, message.messageId, preview.fileId)
                        ?: return false
                replyAlreadySeen(message, previousMessageId)
            }
            is VideoContent -> {
                val thumbnail = content.media.thumbnail ?: return false
                val image = downloadImage(thumbnail.fileId) ?: return false
                val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
                handleVideo(message, content, chatEntity, dHash(image))
            }
            else -> false
        }
    }

    private suspend fun handleVideo(
        message: AccessibleMessage,
        content: VideoContent,
        chatEntity: com.github.djaler.evilbot.entity.Chat,
        thumbHash: Long
    ): Boolean {
        val media = content.media
        val candidates = duplicateMediaChecker.findVideoCandidates(chatEntity, thumbHash, media.duration)
        if (candidates.isEmpty()) {
            duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, null)
            return false
        }

        // Tier 1: дорогая проверка только на коллизии Tier 0
        val newFrames = videoFingerprintService.extractFrameHashes(media.fileId, media.fileSize, media.duration)
        if (newFrames == null) {
            // проверка невозможна (>20МБ / ошибка) → молчим
            duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, null)
            return false
        }

        for (candidate in candidates) {
            val candidateFrames = candidate.frameHashes
                ?: videoFingerprintService.extractFrameHashes(FileId(candidate.fileId), null, candidate.duration)
                    ?.also { duplicateMediaChecker.cacheFrameHashes(candidate, it) }
                ?: continue
            if (framesMatch(newFrames, candidateFrames)) {
                val previousMessageId = duplicateMediaChecker.shiftToCurrent(candidate, message.messageId)
                return replyAlreadySeen(message, previousMessageId)
            }
        }

        // та же обложка+длительность, но другой контент → молчим, кэшируем кадры нового
        duplicateMediaChecker.recordVideo(thumbHash, chatEntity, message.messageId, media.fileId, media.duration, newFrames)
        return false
    }

    private suspend fun downloadImage(fileId: FileId): BufferedImage? {
        return try {
            val bytes = requestExecutor.downloadFile(fileId)
            ByteArrayInputStream(bytes).use { ImageIO.read(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to download or decode media, skipping duplicate check", e)
            sentryClient.captureException(e)
            null
        }
    }

    private suspend fun replyAlreadySeen(message: AccessibleMessage, previousMessageId: Long): Boolean {
        val messageLink = makeLinkToMessage(message.chat, MessageId(previousMessageId)) ?: return false
        requestExecutor.reply(
            message,
            buildEntities {
                +"Уже было - " + link(messageLink)
            }
        )
        return true
    }
}
```

- [ ] **Step 2: Build the whole project + run all tests**

Run: `./gradlew build 2>&1 | tee /tmp/test-output.log`
Expected: BUILD SUCCESSFUL; `VideoFingerprintTest` and `DHashTest` pass.

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt
git commit -m "feat: cascade video duplicate detection via thumbnail+duration then keyframes"
```

- [ ] **Step 4: Manual verification (live bot / staging)**

Run the bot (`./gradlew bootRun` with a test token, or deploy to staging) and confirm in a group chat:
1. Post a video → repost the **same** video → bot replies "Уже было" (Tier 0 collides, Tier 1 confirms).
2. Post two **different** videos that both have a black first frame **and** the same duration → bot stays silent on the second (Tier 0 collides, Tier 1 rejects). This is the bug being fixed.
3. Post two different videos with different durations → bot stays silent (Tier 0 rejects, no download — check logs show no ffmpeg run).
4. Post a photo, repost it → still replies "Уже было" (photo path unchanged).
5. Post a video > 20 MB twice → bot stays silent (Tier 1 impossible → silent).

---

### Task 6: Update project docs

Record the new config-free behavior and the new service in `CLAUDE.md` per the standing rule to update it when architecture changes.

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Update the architecture/clients notes**

In `CLAUDE.md`, in the `### Database` paragraph (right after the Redis sentence), append this line, matching the file's terse style:

```markdown
Video duplicate detection is a two-tier cascade: thumbnail perceptual hash + `duration` gate (`DuplicateMediaChecker`), then ffmpeg keyframe comparison (`VideoFingerprintService`) only on collisions; `media_hashes` carries `duration` and `frame_hashes` columns.
```

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: note cascade video duplicate detection in CLAUDE.md"
```

---

## Notes for the implementer

- **`Chat` entity import in the handler:** the snippet fully-qualifies `com.github.djaler.evilbot.entity.Chat` to avoid clashing with tgbotapi's `Chat`. Replace with an aliased import if you prefer (`import com.github.djaler.evilbot.entity.Chat as ChatEntity`), matching how the codebase already disambiguates these two `Chat` types.
- **`FileId(candidate.fileId)`** reconstructs a downloadable `FileId` from the stored string. `FileId` wraps a `String`; if its constructor differs in 33.1.0, build the value the same way the entity stored it (`preview.fileId.fileId`).
- **Candidate size unknown:** when re-extracting a candidate's frames we pass `fileSize = null`, so a candidate > 20 MB fails at `downloadFile` (caught → `null` → candidate skipped). Acceptable; such a candidate simply never gets a cached vector.
- **`dHash` visibility:** `dHash` is `internal` and lives in `DuplicateMediaChecker.kt` in package `...service`; the handler is in `...handlers`, same Gradle module, so `internal` is visible. Import it explicitly.
```