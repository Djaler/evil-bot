package com.github.djaler.evilbot.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

enum class PartOfSpeech(val mystemCode: String) {
    ADJECTIVE("A"),
    NOUN("S"),
    PREPOSITION("PR"),
    CONJUNCTION("CONJ"),
    PARTICLE("PART");

    companion object {
        private val byCode = entries.associateBy { it.mystemCode }

        fun fromMystemCode(code: String): PartOfSpeech? = byCode[code]
    }
}

data class WordAnalysis(
    val text: String,
    val partsOfSpeech: Set<PartOfSpeech>
)

@Service
class MystemService(
    private val objectMapper: ObjectMapper
) {
    companion object {
        private val log = LogManager.getLogger()
        private const val TIMEOUT_MS = 5000L

        private val mystemAvailable: Boolean by lazy {
            try {
                ProcessBuilder("mystem", "-v")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                false
            }
        }
    }

    fun analyze(text: String): List<WordAnalysis>? {
        if (!mystemAvailable) return null

        try {
            val process = ProcessBuilder("mystem", "-gi", "--format", "json", "--eng-gr")
                .redirectErrorStream(true)
                .start()

            val outputFuture = CompletableFuture.supplyAsync {
                process.inputStream.bufferedReader().use { it.readText() }
            }

            process.outputStream.bufferedWriter().use { it.write(text) }

            val finished = process.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                log.warn("mystem timeout after {} ms", TIMEOUT_MS)
                return null
            }

            if (process.exitValue() != 0) {
                log.warn("mystem exited with code {}", process.exitValue())
                return null
            }

            val output = outputFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS)

            val entries: List<MystemEntry> = objectMapper.readValue(output)

            return entries
                .filter { entry -> entry.text.any { it.isLetter() } }
                .map { entry ->
                    val posSet = entry.analysis
                        .mapNotNull { extractPartOfSpeech(it.gr) }
                        .toSet()
                    WordAnalysis(text = entry.text, partsOfSpeech = posSet)
                }
        } catch (e: Exception) {
            log.warn("mystem analysis failed", e)
            return null
        }
    }

    private fun extractPartOfSpeech(gr: String): PartOfSpeech? {
        // gr format: "S,m,anim=nom,sg" or "A=(acc,sg,plen,m)" or "PR="
        // POS code is the first token before '=' or ','
        val code = gr.substringBefore('=').split(',').firstOrNull()?.trim() ?: return null
        return PartOfSpeech.fromMystemCode(code)
    }

    private data class MystemAnalysis(val gr: String)
    private data class MystemEntry(val text: String, val analysis: List<MystemAnalysis> = emptyList())
}
