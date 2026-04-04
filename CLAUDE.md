# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kotlin/Spring Boot Telegram bot ("Evil Bot") using `dev.inmo:tgbotapi`. Backed by PostgreSQL + Redis, deployed via Docker Compose.

## Build & Run

Java 11 is required (see Dockerfile and CI). Determine JAVA_HOME before first Gradle run.

```bash
./gradlew build        # Build + run tests
./gradlew bootRun      # Run locally
./gradlew bootJar      # Create executable JAR
```

No tests exist currently (`src/test` is empty).

## Architecture

### Handler Chain Pattern

All Telegram updates flow through `UpdatesManager`, which iterates over a sorted list of `UpdateHandler` implementations. Each handler returns `true` if it handled the update (stops the chain) or `false` to pass to the next handler.

**Handler base classes** (`handlers/base/`):
- `UpdateHandler` — interface with `handleUpdate()`, `updateType`, and `order` (default 1)
- `CommandHandler` — abstract class for `/command` handlers; parses command args, registers bot commands with scopes
- `CommonMessageHandler`, `MessageHandler` — handle general messages with filters
- `CallbackQueryHandler`, `NewMemberHandler`, `PollAnswerHandler` — specialized handlers

**Adding a new command**: extend `CommandHandler`, provide command name(s), description, and scope. Implement `handleCommand(message, args)`.

**Command scopes** control where the command appears in Telegram's command menu. Set via `commandScope` parameter in `CommandHandler` constructor:
- `BotCommandScope.Default` — visible everywhere (default)
- `BotCommandScope.AllGroupChats` — only in groups
- `BotCommandScope.AllChatAdministrators` — only for group admins
- `BotCommandScopeChat(chatId)` — only in a specific chat (e.g. private chat with a specific user)

Commands are registered in `BotInitializer.updateCommands()` via `setMyCommands()` per scope. `CommandService.normalizeCommands()` handles scope inheritance (`AllChatAdministrators` inherits from `AllGroupChats`, which inherits from `Default`).

### Layers

- `handlers/` — Telegram update handlers (commands in `handlers/commands/`)
- `service/` — business logic
- `repository/` — Spring Data JPA repositories
- `entity/` — JPA entities
- `clients/` — external API clients (Fixer currency, LocationIQ, VK Cloud voice, Yandex GPT, CAS anti-spam)
- `filters/` — message/query filter predicates composable with `and`/`or`
- `components/` — Spring components (`UpdatesManager`, `BotInitializer`, `ExceptionsManager`, schedulers)

### Database

PostgreSQL with Flyway migrations in `src/main/resources/db/migration/`. Redis for caching (1h default TTL).

### Configuration

All config via `application.properties` + environment variables. Custom properties use `@ConfigurationProperties` + `@ConstructorBinding` data classes (registered in `@EnableConfigurationProperties` in `Application.kt`). Key properties: `telegram.bot.token`, `backup.admin-telegram-id`, `backup.cron`, `fixer.api.key`, `locationiq.api.key`, `vk.api.key`, `yandex.api.token`, `video.download.enabled`.

### Scheduled Tasks

`@EnableScheduling` is active. Schedulers in `components/` use `@Scheduled` with `GlobalScope.launch` for coroutine support. External processes (ffmpeg, yt-dlp) are run via `ProcessBuilder`.

### Deployment

- Docker Compose: `docker-compose up` (bot + PostgreSQL + Redis)
- CI: GitHub Actions — `./gradlew build` on PRs, SSH deploy on push to master