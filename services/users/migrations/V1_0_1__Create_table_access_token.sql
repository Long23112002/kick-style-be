CREATE TABLE users.access_token
(
    id         BIGSERIAL PRIMARY KEY,
    token      TEXT      NOT NULL,
    user_id    BIGINT    NOT NULL,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked    BOOLEAN   DEFAULT FALSE
);
