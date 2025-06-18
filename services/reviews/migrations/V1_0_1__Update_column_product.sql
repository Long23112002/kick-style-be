ALTER TABLE reviews.reviews
    ADD COLUMN IF NOT EXISTS product_id BIGINT;