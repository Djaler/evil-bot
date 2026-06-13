# Duplicate Media Detection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Заменить ненадёжный MD5-детектор дубликатов картинок на перцептивный dHash с поиском по расстоянию Хэмминга и расширить детекцию на видео и GIF.

**Architecture:** `SeenMemeHandler` извлекает из сообщения `PhotoSize` (самое маленькое фото / thumbnail видео / thumbnail гифки), скачивает его, считает 64-битный dHash и ищет в новой таблице `media_hashes` запись того же чата с расстоянием Хэмминга ≤ 5 (`bit_count(hash # :hash)` в Postgres 15). Старая таблица `image_hashes` не трогается (удалится отдельной миграцией после проверки в проде), старые сущность/репозиторий/сервис удаляются.

**Tech Stack:** Kotlin, Spring Boot 3.4, Spring Data JPA (native query), Flyway, PostgreSQL 15, tgbotapi 33.1.0, Java2D (JDK), JUnit 5 + MockK + Kotest assertions (без Spring-контекста в тестах).

**Spec:** `docs/superpowers/specs/2026-06-12-duplicate-media-detection-design.md`

**Перед любым запуском Gradle:**

```bash
export JAVA_HOME=/Users/ki.romanov/Library/Java/JavaVirtualMachines/corretto-17.0.11/Contents/Home
```

Ветка — `master`, task-кода нет, поэтому conventional commits без скоупа. Никаких `Co-Authored-By`.

---

## File Structure

| Файл | Действие | Ответственность |
|---|---|---|
| `build.gradle.kts` | изменить | тестовые зависимости |
| `src/test/kotlin/com/github/djaler/evilbot/service/DHashTest.kt` | создать | тесты dHash |
| `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt` | создать | dHash + сервис поиска/сохранения хешей |
| `src/main/resources/db/migration/V10__add_media_hashes_table.sql` | создать | новая таблица |
| `src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt` | создать | JPA-сущность |
| `src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt` | создать | поиск по Хэммингу |
| `src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt` | переписать | извлечение PhotoSize из фото/видео/GIF |
| `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateImageChecker.kt` | удалить | заменён `DuplicateMediaChecker` |
| `src/main/kotlin/com/github/djaler/evilbot/entity/ImageHash.kt` | удалить | заменён `MediaHash` |
| `src/main/kotlin/com/github/djaler/evilbot/repository/ImageHashRepository.kt` | удалить | заменён `MediaHashRepository` |
| `CLAUDE.md` | изменить | «тестов нет» больше не верно |

---

### Task 1: Тестовая инфраструктура

В проекте нет ни тестовых зависимостей, ни `src/test`. Тестируем без Spring Boot: чистый JUnit 5 + MockK + Kotest для ассертов, Spring-контекст в тестах не поднимается.

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Добавить тестовые зависимости и junit platform**

В блок `dependencies` (после строки `implementation("io.github.resilience4j:resilience4j-ratelimiter:2.2.0")`):

```kotlin
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
```

Версия `junit-jupiter` управляется Spring BOM через io.spring.dependency-management. MockK и Kotest в BOM нет — версии явные (при реализации допустимо поднять до свежих стабильных).

После блока `tasks.withType<KotlinCompile>` добавить:

```kotlin
tasks.test {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Проверить, что test-таска работает**

```bash
./gradlew test 2>&1 | tee /tmp/test-output.log
```

Expected: `BUILD SUCCESSFUL` (тестов пока нет — таска проходит пустой).

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "build: add test infrastructure"
```

---

### Task 2: dHash (TDD)

Функция dHash — internal top-level в файле будущего сервиса, чтобы тестировать её без моков репозитория (класс сервиса появится в Task 4 в этом же файле).

**Files:**
- Create: `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt`
- Test: `src/test/kotlin/com/github/djaler/evilbot/service/DHashTest.kt`

- [ ] **Step 1: Написать падающие тесты**

Создать `src/test/kotlin/com/github/djaler/evilbot/service/DHashTest.kt`:

```kotlin
package com.github.djaler.evilbot.service

import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ComponentColorModel
import java.awt.image.DataBuffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.stream.MemoryCacheImageOutputStream
import kotlin.random.Random

class DHashTest {

    @Test
    fun `recompressed copy is close to original`() {
        val original = testImage(seed = 1)
        val recompressed = recompressJpeg(original, quality = 0.3f)

        val distance = hammingDistance(dHash(original), dHash(recompressed))

        distance shouldBeLessThanOrEqual 5
    }

    @Test
    fun `resized copy is close to original`() {
        val original = testImage(seed = 1)
        val resized = resize(original, 200, 150)

        val distance = hammingDistance(dHash(original), dHash(resized))

        distance shouldBeLessThanOrEqual 5
    }

    @Test
    fun `different images are far apart`() {
        val distance = hammingDistance(dHash(testImage(seed = 1)), dHash(testImage(seed = 2)))

        distance shouldBeGreaterThan 10
    }

    @Test
    fun `solid color image hashes without error`() {
        dHash(BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB))
    }

    @Test
    fun `custom type image hashes without error`() {
        val image = customTypeImage()
        image.type shouldBe BufferedImage.TYPE_CUSTOM

        dHash(image)
    }

    private fun hammingDistance(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)

    private fun testImage(seed: Int): BufferedImage {
        val random = Random(seed)
        val image = BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        repeat(30) {
            graphics.color = Color(random.nextInt(0xFFFFFF))
            graphics.fillRect(
                random.nextInt(280),
                random.nextInt(200),
                40 + random.nextInt(80),
                40 + random.nextInt(80)
            )
        }
        graphics.dispose()
        return image
    }

    private fun resize(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = resized.createGraphics()
        graphics.drawImage(image, 0, 0, width, height, null)
        graphics.dispose()
        return resized
    }

    private fun recompressJpeg(image: BufferedImage, quality: Float): BufferedImage {
        val writer = ImageIO.getImageWritersByFormatName("jpeg").next()
        val output = ByteArrayOutputStream()
        writer.output = MemoryCacheImageOutputStream(output)
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        writer.write(null, IIOImage(image, null, null), param)
        writer.dispose()
        return ImageIO.read(ByteArrayInputStream(output.toByteArray()))
    }

    private fun customTypeImage(): BufferedImage {
        val colorModel = ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            intArrayOf(16, 16, 16),
            false,
            false,
            Transparency.OPAQUE,
            DataBuffer.TYPE_USHORT
        )
        val raster = colorModel.createCompatibleWritableRaster(50, 50)
        return BufferedImage(colorModel, raster, false, null)
    }
}
```

- [ ] **Step 2: Убедиться, что тесты падают**

```bash
./gradlew test 2>&1 | tee /tmp/test-output.log
```

Expected: FAIL — компиляция тестов падает с `unresolved reference: dHash`.

- [ ] **Step 3: Реализовать dHash**

Создать `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt`:

```kotlin
package com.github.djaler.evilbot.service

import java.awt.Image
import java.awt.image.BufferedImage

/**
 * Перцептивный difference hash: 9x8 в оттенках серого, бит = «левый пиксель ярче правого».
 * Канва фиксированного TYPE_INT_RGB — исходники с TYPE_CUSTOM (CMYK-JPEG) не падают.
 */
internal fun dHash(image: BufferedImage): Long {
    val scaled = image.getScaledInstance(9, 8, Image.SCALE_SMOOTH)
    val resized = BufferedImage(9, 8, BufferedImage.TYPE_INT_RGB)
    val graphics = resized.createGraphics()
    graphics.drawImage(scaled, 0, 0, null)
    graphics.dispose()

    var hash = 0L
    for (y in 0 until 8) {
        for (x in 0 until 8) {
            if (gray(resized.getRGB(x, y)) > gray(resized.getRGB(x + 1, y))) {
                hash = hash or (1L shl (y * 8 + x))
            }
        }
    }
    return hash
}

private fun gray(rgb: Int): Int {
    val r = rgb shr 16 and 0xFF
    val g = rgb shr 8 and 0xFF
    val b = rgb and 0xFF
    return (r * 299 + g * 587 + b * 114) / 1000
}
```

Примечание: дизайн описывает «ресайз с билинейной интерполяцией»; `Image.SCALE_SMOOTH` — это area-averaging, который на цели 9×8 устойчивее однопроходного билинейного (тот сэмплирует редкие пиксели большого исходника). Интент дизайна — «гладкое уменьшение на канву фиксированного типа» — сохранён.

- [ ] **Step 4: Убедиться, что тесты проходят**

```bash
./gradlew test 2>&1 | tee /tmp/test-output.log
```

Expected: `BUILD SUCCESSFUL`, 5 tests passed. Если `different images are far apart` дал расстояние ≤ 10 (картинки с seed 1 и 2 случайно похожи) — поменять seed второй картинки на 3 и перезапустить; порог не трогать.

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt src/test/kotlin/com/github/djaler/evilbot/service/DHashTest.kt
git commit -m "feat: add dHash perceptual hash function"
```

---

### Task 3: Таблица, сущность, репозиторий

**Files:**
- Create: `src/main/resources/db/migration/V10__add_media_hashes_table.sql`
- Create: `src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt`
- Create: `src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt`

- [ ] **Step 1: Миграция**

Создать `src/main/resources/db/migration/V10__add_media_hashes_table.sql`:

```sql
-- Замена image_hashes: перцептивный dHash (BIGINT) вместо MD5, без UNIQUE —
-- поиск дубликатов идёт по расстоянию Хэмминга, а не по равенству.
-- image_hashes не трогаем: удаляется отдельной миграцией после проверки фичи в проде.
CREATE TABLE media_hashes
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hash       BIGINT   NOT NULL,
    chat_id    SMALLINT NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    message_id BIGINT   NOT NULL,
    file_id    TEXT     NOT NULL
);

CREATE INDEX media_hashes_chat_id_idx ON media_hashes (chat_id);
```

- [ ] **Step 2: Сущность**

Создать `src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt` (по образцу `ImageHash.kt`):

```kotlin
package com.github.djaler.evilbot.entity

import jakarta.persistence.*

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0
)
```

- [ ] **Step 3: Репозиторий с поиском по Хэммингу**

Создать `src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt`:

```kotlin
package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.MediaHash
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MediaHashRepository : JpaRepository<MediaHash, Int> {
    /**
     * '#' — XOR в Postgres, bit_count — число единичных битов (PG14+).
     * Линейный скан внутри чата; при росте до сотен тысяч строк на чат
     * см. pigeonhole-декомпозицию в дизайн-доке.
     */
    @Query(
        value = "SELECT * FROM media_hashes WHERE chat_id = :chatId AND bit_count(hash # :hash) <= :maxDistance ORDER BY id LIMIT 1",
        nativeQuery = true
    )
    fun findByChatIdAndHashCloseTo(
        @Param("chatId") chatId: Short,
        @Param("hash") hash: Long,
        @Param("maxDistance") maxDistance: Int
    ): MediaHash?
}
```

- [ ] **Step 4: Проверить компиляцию**

```bash
./gradlew compileKotlin
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/migration/V10__add_media_hashes_table.sql src/main/kotlin/com/github/djaler/evilbot/entity/MediaHash.kt src/main/kotlin/com/github/djaler/evilbot/repository/MediaHashRepository.kt
git commit -m "feat: add media_hashes table, entity and repository"
```

---

### Task 4: Сервис DuplicateMediaChecker

**Files:**
- Modify: `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt`

- [ ] **Step 1: Добавить класс сервиса в файл с dHash**

В начало файла (после `package`) добавить импорты и класс **перед** функцией `dHash`:

```kotlin
package com.github.djaler.evilbot.service

import com.github.djaler.evilbot.entity.Chat
import com.github.djaler.evilbot.entity.MediaHash
import com.github.djaler.evilbot.repository.MediaHashRepository
import dev.inmo.tgbotapi.requests.abstracts.FileId
import dev.inmo.tgbotapi.types.MessageId
import org.springframework.stereotype.Component
import java.awt.Image
import java.awt.image.BufferedImage

@Component
class DuplicateMediaChecker(
    private val mediaHashRepository: MediaHashRepository,
) {
    companion object {
        private const val MAX_HAMMING_DISTANCE = 5
    }

    fun findDuplicate(image: BufferedImage, chat: Chat): Long? {
        val duplicate = mediaHashRepository.findByChatIdAndHashCloseTo(chat.id, dHash(image), MAX_HAMMING_DISTANCE)

        return duplicate?.messageId
    }

    fun saveHash(image: BufferedImage, chat: Chat, messageId: MessageId, fileId: FileId) {
        mediaHashRepository.save(
            MediaHash(
                dHash(image),
                chat.id,
                messageId.long,
                fileId.fileId
            )
        )
    }
}
```

Функция `dHash` и `gray` остаются в этом же файле без изменений.

- [ ] **Step 2: Проверить компиляцию и тесты**

```bash
./gradlew test 2>&1 | tee /tmp/test-output.log
```

Expected: `BUILD SUCCESSFUL`, 5 tests passed.

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/service/DuplicateMediaChecker.kt
git commit -m "feat: add DuplicateMediaChecker service"
```

---

### Task 5: Хендлер + удаление старого кода

**Files:**
- Modify: `src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt` (полная замена содержимого)
- Delete: `src/main/kotlin/com/github/djaler/evilbot/service/DuplicateImageChecker.kt`
- Delete: `src/main/kotlin/com/github/djaler/evilbot/entity/ImageHash.kt`
- Delete: `src/main/kotlin/com/github/djaler/evilbot/repository/ImageHashRepository.kt`

- [ ] **Step 1: Переписать SeenMemeHandler**

Полное новое содержимое `src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt`:

```kotlin
package com.github.djaler.evilbot.handlers

import com.github.djaler.evilbot.handlers.base.MessageHandler
import com.github.djaler.evilbot.service.ChatService
import com.github.djaler.evilbot.service.DuplicateMediaChecker
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.files.downloadFile
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asPublicChat
import dev.inmo.tgbotapi.extensions.utils.formatting.makeLinkToMessage
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.content.AnimationContent
import dev.inmo.tgbotapi.types.message.content.PhotoContent
import dev.inmo.tgbotapi.types.message.content.VideoContent
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.link
import kotlinx.coroutines.CancellationException
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
class SeenMemeHandler(
    private val requestExecutor: RequestsExecutor,
    private val duplicateMediaChecker: DuplicateMediaChecker,
    private val chatService: ChatService,
) : MessageHandler() {
    companion object {
        private val log = LogManager.getLogger()
    }

    override suspend fun handleMessage(message: AccessibleMessage): Boolean {
        val chat = message.chat.asPublicChat() ?: return false
        val content = message.asContentMessage()?.content ?: return false

        // для хеша 9x8 хватает мини-превью: самое маленькое фото или thumbnail видео/гифки
        val photoSize = when (content) {
            is PhotoContent -> content.mediaCollection.minByOrNull { it.resolution }
            is VideoContent -> content.media.thumbnail
            is AnimationContent -> content.media.thumbnail
            else -> null
        } ?: return false

        val image = try {
            val bytes = requestExecutor.downloadFile(photoSize)
            ByteArrayInputStream(bytes).use { ImageIO.read(it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Failed to download or decode media, skipping duplicate check", e)
            return false
        } ?: return false

        val (chatEntity, _) = chatService.getOrCreateChatFrom(chat)
        val originalMessageId = duplicateMediaChecker.findDuplicate(image, chatEntity)

        if (originalMessageId == null) {
            duplicateMediaChecker.saveHash(image, chatEntity, message.messageId, photoSize.fileId)
            return false
        } else {
            val messageLink = makeLinkToMessage(message.chat, MessageId(originalMessageId)) ?: return false

            requestExecutor.reply(
                message,
                buildEntities {
                    +"Уже было - " + link(messageLink)
                }
            )
            return true
        }
    }
}
```

- [ ] **Step 2: Удалить старый код**

```bash
git rm src/main/kotlin/com/github/djaler/evilbot/service/DuplicateImageChecker.kt src/main/kotlin/com/github/djaler/evilbot/entity/ImageHash.kt src/main/kotlin/com/github/djaler/evilbot/repository/ImageHashRepository.kt
```

- [ ] **Step 3: Полная сборка**

```bash
./gradlew build 2>&1 | tee /tmp/test-output.log
```

Expected: `BUILD SUCCESSFUL` (компиляция + 5 тестов). Если компиляция падает на удалённых классах — найти оставшиеся ссылки `grep -rn "DuplicateImageChecker\|ImageHash" src/main` и переключить их на новые классы.

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/com/github/djaler/evilbot/handlers/SeenMemeHandler.kt
git commit -m "feat: switch duplicate detection to perceptual hash and cover video/GIF"
```

---

### Task 6: Документация

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Обновить CLAUDE.md**

Заменить строку:

```
No tests exist currently (`src/test` is empty).
```

на:

```
Tests live in `src/test/kotlin` (JUnit 5 + MockK + Kotest assertions, no Spring context). Run with `./gradlew test`.
```

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md with test infrastructure"
```

---

## Ручная проверка (после реализации)

Авто-тестов на SQL-запрос нет (Testcontainers не добавляем). Проверить на живом стенде:

1. `docker-compose up` — Flyway применит V10.
2. Отправить в тестовый чат фото → переслать его же → бот отвечает «Уже было».
3. Сохранить фото на диск, отправить как новую загрузку → «Уже было» (главный кейс, который не работал).
4. Отправить видео дважды → «Уже было» на второй раз; то же с гифкой.
5. Проверить лог на отсутствие ошибок `bit_count`/маппинга `MediaHash`.
