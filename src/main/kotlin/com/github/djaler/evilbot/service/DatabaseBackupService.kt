package com.github.djaler.evilbot.service

import org.apache.logging.log4j.LogManager
import org.postgresql.copy.CopyManager
import org.postgresql.jdbc.PgConnection
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import javax.sql.DataSource

@Service
class DatabaseBackupService(
    private val dataSource: DataSource
) {
    companion object {
        private val log = LogManager.getLogger()
        private const val EXCLUDED_TABLE = "flyway_schema_history"
    }

    fun createDump(): File {
        val file = Files.createTempFile("backup-", ".sql").toFile()

        try {
            dataSource.connection.use { connection ->
                val pgConnection = connection.unwrap(PgConnection::class.java)
                val copyManager = CopyManager(pgConnection)
                val tables = getTableNames(pgConnection)

                log.info("Backing up {} tables: {}", tables.size, tables)

                file.bufferedWriter().use { writer ->
                    writer.write("-- evil-bot database backup\n\n")

                    for (table in tables) {
                        val columns = getColumnNames(pgConnection, table)
                        val columnList = columns.joinToString(", ")

                        writer.write("COPY $table ($columnList) FROM stdin;\n")
                        writer.flush()

                        val buffer = ByteArrayOutputStream()
                        copyManager.copyOut("COPY $table ($columnList) TO STDOUT", buffer)
                        writer.write(buffer.toString(Charsets.UTF_8.name()))

                        writer.write("\\.\n\n")
                    }
                }
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }

        return file
    }

    private fun getTableNames(connection: PgConnection): List<String> {
        val tables = mutableListOf<String>()
        connection.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename != '$EXCLUDED_TABLE' ORDER BY tablename"
            ).use { rs ->
                while (rs.next()) {
                    tables.add(rs.getString("tablename"))
                }
            }
        }
        return tables
    }

    private fun getColumnNames(connection: PgConnection, table: String): List<String> {
        val columns = mutableListOf<String>()
        connection.createStatement().use { stmt ->
            stmt.executeQuery(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '$table' ORDER BY ordinal_position"
            ).use { rs ->
                while (rs.next()) {
                    columns.add(rs.getString("column_name"))
                }
            }
        }
        return columns
    }
}
