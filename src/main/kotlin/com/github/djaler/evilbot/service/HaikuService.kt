package com.github.djaler.evilbot.service

import org.springframework.stereotype.Service

@Service
class HaikuService(
    private val mystemService: MystemService
) {

    companion object {
        private val RUSSIAN_VOWELS = "аеёиоуыэюя".toSet()
        private val CYRILLIC_WORD_REGEX = Regex("[а-яёА-ЯЁ]+")
        private val HAIKU_PATTERN = listOf(5, 7, 5)

        private val CANNOT_END_LINE = setOf(
            PartOfSpeech.PREPOSITION,
            PartOfSpeech.CONJUNCTION,
            PartOfSpeech.PARTICLE
        )
    }

    fun countSyllables(word: String): Int {
        return word.lowercase().count { it in RUSSIAN_VOWELS }
    }

    fun tryParseHaiku(text: String): List<String>? {
        val words = CYRILLIC_WORD_REGEX.findAll(text).map { it.value }.toList()
        if (words.size < 3) return null

        val totalSyllables = words.sumOf { countSyllables(it) }
        if (totalSyllables != HAIKU_PATTERN.sum()) return null

        val lines = mutableListOf<List<String>>()
        var wordIndex = 0
        for (target in HAIKU_PATTERN) {
            var syllableCount = 0
            val lineWords = mutableListOf<String>()
            while (wordIndex < words.size && syllableCount < target) {
                val word = words[wordIndex]
                syllableCount += countSyllables(word)
                lineWords.add(word)
                wordIndex++
            }
            if (syllableCount != target) return null
            lines.add(lineWords)
        }

        if (wordIndex != words.size) return null

        if (!validateLineBreaks(lines)) return null

        return lines.map { it.joinToString(" ") }
    }

    /**
     * Проверяет, что разрывы между строками хокку не нарушают естественную структуру речи.
     * Использует морфологический анализ MyStem для определения частей речи на границах строк.
     * Если MyStem недоступен (напр. локальная разработка) — пропускаем проверку.
     */
    private fun validateLineBreaks(lines: List<List<String>>): Boolean {
        val allWords = lines.flatten()
        val wordAnalyses = mystemService.analyze(allWords.joinToString(" "))

        // MyStem недоступен или вернул неожиданное количество слов — пропускаем валидацию
        if (wordAnalyses == null || wordAnalyses.size != allWords.size) return true

        var wordOffset = 0
        for (lineIndex in 0 until lines.size - 1) {
            val lastInLine = wordAnalyses[wordOffset + lines[lineIndex].size - 1]
            val firstInNextLine = wordAnalyses[wordOffset + lines[lineIndex].size]

            // Строка не должна заканчиваться служебным словом (предлог, союз, частица) —
            // они всегда относятся к следующему слову: "на | столе" читается неестественно
            if (lastInLine.partsOfSpeech.any { it in CANNOT_END_LINE }) return false

            // Прилагательное в конце строки + существительное в начале следующей —
            // разрыв словосочетания: "красивый | дом" звучит как обрезанная фраза.
            // Проверяем только однозначные прилагательные — MyStem может давать несколько
            // вариантов разбора (напр. "остыл" = глагол или краткое прилагательное)
            val onlyAdjective = lastInLine.partsOfSpeech == setOf(PartOfSpeech.ADJECTIVE)
            val nextStartsWithNoun = PartOfSpeech.NOUN in firstInNextLine.partsOfSpeech
            if (onlyAdjective && nextStartsWithNoun) return false

            wordOffset += lines[lineIndex].size
        }

        return true
    }
}
