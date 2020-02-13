ALTER TABLE captcha_restrictions
    ADD captcha_message_id BIGINT NOT NULL DEFAULT -1;

ALTER TABLE captcha_restrictions
    ALTER COLUMN captcha_message_id DROP DEFAULT;

