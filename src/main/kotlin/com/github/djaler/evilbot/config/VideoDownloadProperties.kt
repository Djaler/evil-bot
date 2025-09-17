package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("video.download")
class VideoDownloadProperties {
    /**
     * Включает/выключает фичу скачивания видео по ссылкам.
     * Если null — считаем выключенной, пока не переопределено в конфиге.
     */
    var enabled: Boolean? = null

    /**
     * Разрешенные домены, с которых можно скачивать видео.
     * Поддомены также считаются валидными (например, www.youtube.com).
     */
    lateinit var allowedDomains: List<String>

    /**
     * Максимальный размер загружаемого файла yt-dlp.
     * Формат как у yt-dlp: например 50M, 100M, 1G.
     */
    lateinit var maxFilesize: String
}
