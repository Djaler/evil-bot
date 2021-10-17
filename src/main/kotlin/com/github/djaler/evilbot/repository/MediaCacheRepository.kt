package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.MediaCache
import org.springframework.data.jpa.repository.JpaRepository

interface MediaCacheRepository : JpaRepository<MediaCache, Int> {
    fun findByDigest(digest: String): MediaCache?
}
