-- Tạo bảng role
CREATE TABLE users.role
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN   DEFAULT FALSE
);

-- Tạo bảng user
CREATE TABLE users.user
(
    id         BIGSERIAL PRIMARY KEY,
    full_name  VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    phone      VARCHAR(20),
    address    TEXT,
    district   VARCHAR(100),
    ward       VARCHAR(100),
    role_id    BIGINT,
    avatar_url TEXT,
    gender     VARCHAR(10),
    is_verify  BOOLEAN   DEFAULT FALSE,
    is_deleted BOOLEAN   DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
