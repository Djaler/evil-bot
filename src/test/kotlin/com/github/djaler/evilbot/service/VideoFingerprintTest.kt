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
    fun `shorter overlap that does not reach majority does not match`() {
        // 1 of 2 frames match — not a strict majority (requires > 1)
        framesMatch(listOf(1L, 0xFFL), listOf(1L, 0x00L), maxDistance = 5) shouldBe false
    }

    @Test
    fun `hammingDistance between identical values is zero`() {
        hammingDistance(0L, 0L) shouldBe 0
    }

    @Test
    fun `hammingDistance counts set bits correctly`() {
        hammingDistance(0L, 0xFFL) shouldBe 8
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
