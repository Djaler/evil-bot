ALTER TABLE dice_poll_captcha_restrictions
    ADD kicked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE dice_poll_captcha_restrictions
    ADD kick_message_id BIGINT;

