package com.github.djaler.evilbot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("video.download")
class VideoDownloadProperties {
    /**
     * Максимальный размер загружаемого файла yt-dlp.
     * Формат как у yt-dlp: например 50M, 100M, 1G.
     */
    var maxFilesize: String = "50M"
}
