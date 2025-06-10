CREATE TABLE reviews.reviews
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    order_id   BIGINT NOT NULL,
    rating     INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment    TEXT,
    images    JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN   DEFAULT FALSE
);

CREATE TABLE reviews.answers
(
    id           BIGSERIAL PRIMARY KEY,
    review_id    BIGINT NOT NULL,
    user_id BIGINT,
    images    JSONB,
    answer       TEXT   NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN   DEFAULT FALSE
);
