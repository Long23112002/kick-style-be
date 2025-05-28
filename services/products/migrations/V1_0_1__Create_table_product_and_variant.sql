CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    slug        VARCHAR(255),
    category_id BIGINT,
    image_urls  JSONB,
    team_id     BIGINT,
    material_id BIGINT,
    season      VARCHAR(100),
    jersey_type VARCHAR(100),
    is_featured BOOLEAN   DEFAULT FALSE,
    code        VARCHAR(100),
    description TEXT,
    price       NUMERIC(10, 2),
    sale_price  NUMERIC(10, 2),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE product_variants
(
    id               BIGSERIAL PRIMARY KEY,
    product_id       BIGINT NOT NULL,
    size             VARCHAR(50),
    price_adjustment NUMERIC(10, 2),
    stock_quantity   INT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted       BOOLEAN   DEFAULT FALSE
);

