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
