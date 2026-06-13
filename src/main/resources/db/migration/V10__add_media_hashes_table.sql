-- Замена image_hashes: перцептивный dHash (BIGINT) вместо MD5, без UNIQUE —
-- поиск дубликатов идёт по расстоянию Хэмминга, а не по равенству.
CREATE TABLE media_hashes
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hash       BIGINT   NOT NULL,
    chat_id    SMALLINT NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    message_id BIGINT   NOT NULL,
    file_id    TEXT     NOT NULL
);

CREATE INDEX media_hashes_chat_id_idx ON media_hashes (chat_id);
