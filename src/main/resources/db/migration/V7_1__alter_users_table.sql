ALTER TABLE users
    ALTER COLUMN telegram_id TYPE BIGINT USING telegram_id::BIGINT;

ALTER TABLE dice_poll_captcha_restrictions
    ALTER COLUMN member_telegram_id TYPE BIGINT USING member_telegram_id::BIGINT;
