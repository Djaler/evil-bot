CREATE TABLE chats
(
    id          SMALLSERIAL PRIMARY KEY,
    title       VARCHAR NOT NULL,
    telegram_id BIGINT  NOT NULL UNIQUE
);

CREATE TABLE blocked_stickerpacks
(
    id      SERIAL PRIMARY KEY,
    chat_id SMALLINT NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    name    VARCHAR  NOT NULL,
    UNIQUE (chat_id, name)
);

CREATE TABLE users
(
    id          SERIAL PRIMARY KEY,
    username    VARCHAR NOT NULL,
    telegram_id INT     NOT NULL UNIQUE
);

CREATE TABLE user_chat_statistics
(
    id             SERIAL PRIMARY KEY,
    chat_id        SMALLINT  NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    user_id        INT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    messages_count INT       NOT NULL,
    last_activity  TIMESTAMP NOT NULL,
    UNIQUE (chat_id, user_id)
);
