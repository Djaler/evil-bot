-- last_seen_at: момент последнего появления медиа. Обновляется при каждом повторе
-- (скользящее окно TTL), по нему шедулер удаляет давно не встречавшиеся хеши.
ALTER TABLE media_hashes ADD COLUMN last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now();
