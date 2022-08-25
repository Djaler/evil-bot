package com.github.djaler.evilbot.repository

import com.github.djaler.evilbot.entity.CrowdSourcing
import org.springframework.data.jpa.repository.JpaRepository

interface CrowdSourcingRepository : JpaRepository<CrowdSourcing, Short>
