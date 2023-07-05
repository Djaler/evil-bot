ALTER TABLE dice_poll_captcha_restrictions
    ALTER COLUMN can_send_messages DROP NOT NULL,
    ALTER COLUMN can_send_polls DROP NOT NULL,
    ALTER COLUMN can_send_other_messages DROP NOT NULL,
    ALTER COLUMN can_add_web_page_previews DROP NOT NULL,
    ALTER COLUMN can_change_info DROP NOT NULL,
    ALTER COLUMN can_invite_users DROP NOT NULL,
    ALTER COLUMN can_pin_messages DROP NOT NULL;

ALTER TABLE dice_poll_captcha_restrictions DROP COLUMN can_send_media_messages;

ALTER TABLE dice_poll_captcha_restrictions
    ADD can_send_audios BOOLEAN,
    ADD can_send_documents BOOLEAN,
    ADD can_send_photos BOOLEAN,
    ADD can_send_videos BOOLEAN,
    ADD can_send_video_notes BOOLEAN,
    ADD can_send_voice_notes BOOLEAN;

