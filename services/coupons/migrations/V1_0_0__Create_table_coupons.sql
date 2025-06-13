-- Tạo schema coupons nếu chưa có
CREATE SCHEMA IF NOT EXISTS coupons;

-- Tạo bảng coupons
CREATE TABLE coupons.coupons
(
    id               BIGSERIAL PRIMARY KEY,
    code             VARCHAR(50)   NOT NULL UNIQUE,
    description      TEXT,
    discount_type    VARCHAR(20)   NOT NULL  ,
    discount_value   NUMERIC(10,2) NOT NULL,
    minimum_amount   NUMERIC(10,2) DEFAULT 0,
    maximum_discount NUMERIC(10,2),
    usage_limit      INTEGER       DEFAULT NULL,
    used_count       INTEGER       DEFAULT 0,
    start_date       TIMESTAMP     NOT NULL,
    end_date         TIMESTAMP     NOT NULL,
    is_active        BOOLEAN       DEFAULT TRUE,
    created_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    is_deleted       BOOLEAN       DEFAULT FALSE
);

-- Tạo index để tối ưu query
CREATE INDEX idx_coupons_code ON coupons.coupons(code);
CREATE INDEX idx_coupons_active_dates ON coupons.coupons(is_active, start_date, end_date);
CREATE INDEX idx_coupons_is_deleted ON coupons.coupons(is_deleted);

-- Thêm constraint kiểm tra logic
ALTER TABLE coupons.coupons 
ADD CONSTRAINT check_dates CHECK (end_date > start_date);

ALTER TABLE coupons.coupons 
ADD CONSTRAINT check_discount_value CHECK (discount_value > 0);

ALTER TABLE coupons.coupons 
ADD CONSTRAINT check_usage_limit CHECK (usage_limit IS NULL OR usage_limit > 0);

-- Thêm dữ liệu mẫu
INSERT INTO coupons.coupons (code, description, discount_type, discount_value, minimum_amount, maximum_discount, usage_limit, start_date, end_date) VALUES
('WELCOME10', 'Giảm 10% cho khách hàng mới', 'PERCENTAGE', 10, 200000, 50000, 100, NOW(), NOW() + INTERVAL '30 days'),
('SAVE50K', 'Giảm 50,000đ cho đơn từ 500,000đ', 'FIXED_AMOUNT', 50000, 500000, NULL, 50, NOW(), NOW() + INTERVAL '15 days'),
('FREESHIP', 'Miễn phí vận chuyển', 'FIXED_AMOUNT', 30000, 100000, NULL, NULL, NOW(), NOW() + INTERVAL '7 days'); 