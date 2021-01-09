CREATE TABLE dice_poll_captcha_restrictions
(
    id                        SERIAL PRIMARY KEY,
    chat_id                   SMALLINT  NOT NULL REFERENCES chats (id) ON DELETE CASCADE,
    member_telegram_id        INT       NOT NULL,
    date_time                 TIMESTAMP NOT NULL,
    join_message_id           BIGINT    NOT NULL,
    dice_message_id           BIGINT    NOT NULL,
    poll_message_id           BIGINT    NOT NULL,
    poll_id                   VARCHAR   NOT NULL,
    correct_answer_index      SMALLINT  NOT NULL,
    can_send_messages         BOOLEAN   NOT NULL,
    can_send_media_messages   BOOLEAN   NOT NULL,
    can_send_polls            BOOLEAN   NOT NULL,
    can_send_other_messages   BOOLEAN   NOT NULL,
    can_add_web_page_previews BOOLEAN   NOT NULL,
    can_change_info           BOOLEAN   NOT NULL,
    can_invite_users          BOOLEAN   NOT NULL,
    can_pin_messages          BOOLEAN   NOT NULL,
    UNIQUE (chat_id, member_telegram_id)
);
