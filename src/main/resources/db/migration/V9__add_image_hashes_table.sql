CREATE TABLE image_hashes
(
    id         SERIAL PRIMARY KEY,
    hash       VARCHAR  NOT NULL,
    chat_id    SMALLINT NOT NULL,
    message_id BIGINT   NOT NULL,
    file_id    VARCHAR  NOT NULL,

    UNIQUE (chat_id, hash)
);
