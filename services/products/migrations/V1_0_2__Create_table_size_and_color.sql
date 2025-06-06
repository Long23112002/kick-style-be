-- Tạo bảng sizes
CREATE TABLE products.sizes
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN   DEFAULT FALSE
);

-- Tạo bảng colors
CREATE TABLE products.colors
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN   DEFAULT FALSE
);


ALTER TABLE products.product_variants
    ADD COLUMN size_id BIGINT,
    ADD COLUMN color_id BIGINT,
    ADD COLUMN status VARCHAR;
=
ALTER TABLE products.products
    ADD COLUMN status VARCHAR;
