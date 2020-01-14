CREATE TABLE captcha_restrictions
(
    id                 SERIAL PRIMARY KEY,
    chat_id            SMALLINT  NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    member_telegram_id INT       NOT NULL,
    date_time          TIMESTAMP NOT NULL,
    UNIQUE (chat_id, member_telegram_id)
);
