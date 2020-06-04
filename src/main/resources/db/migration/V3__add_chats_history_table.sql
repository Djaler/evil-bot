CREATE TABLE chats_history
(
    id         SERIAL PRIMARY KEY,
    chat_id    SMALLINT NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    join_date  TIMESTAMP,
    leave_date TIMESTAMP
);
