CREATE TABLE media_cache
(
    id      SERIAL PRIMARY KEY,
    digest  VARCHAR NOT NULL UNIQUE,
    file_id VARCHAR NOT NULL
);
