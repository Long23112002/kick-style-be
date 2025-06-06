-- Tạo schema orders nếu chưa có
CREATE SCHEMA IF NOT EXISTS orders;

-- Tạo bảng orders
CREATE TABLE orders.orders
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT        NOT NULL,
    code              VARCHAR(50)   NOT NULL UNIQUE,
    status            VARCHAR(30)   NOT NULL DEFAULT 'PENDING' 
                      CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED', 'REFUNDED')),
    customer_name     VARCHAR(255)  NOT NULL,
    customer_email    VARCHAR(255)  NOT NULL,
    customer_phone    VARCHAR(20)   NOT NULL,
    shipping_address  TEXT          NOT NULL,
    shipping_district VARCHAR(255),
    shipping_ward     VARCHAR(255),
    subtotal          NUMERIC(12,2) NOT NULL DEFAULT 0, -- Tổng tiền hàng chưa tính ship
    discount_amount   NUMERIC(12,2) DEFAULT 0,          -- Số tiền được giảm
    total_amount      NUMERIC(12,2) NOT NULL DEFAULT 0, -- Tổng tiền cuối cùng
    payment_method_id BIGINT        NOT NULL,
    payment_status    VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                      CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
    note              TEXT,
    created_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    is_deleted        BOOLEAN       DEFAULT FALSE
);

-- Tạo bảng order_items
CREATE TABLE orders.order_items
(
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT        NOT NULL,
    variant_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL, -- Lưu tên sản phẩm tại thời điểm đặt hàng
    variant_info JSONB,                  -- Thông tin variant (size, color) tại thời điểm đặt hàng
    quantity     INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(10,2) NOT NULL, -- Giá tại thời điểm đặt hàng
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Tạo index để tối ưu query
CREATE INDEX idx_orders_user_id ON orders.orders(user_id);
CREATE INDEX idx_orders_code ON orders.orders(code);
CREATE INDEX idx_orders_status ON orders.orders(status);
CREATE INDEX idx_orders_payment_status ON orders.orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders.orders(created_at);
CREATE INDEX idx_orders_is_deleted ON orders.orders(is_deleted);

CREATE INDEX idx_order_items_order_id ON orders.order_items(order_id);
CREATE INDEX idx_order_items_variant_id ON orders.order_items(variant_id);

-- Thêm foreign key constraints
ALTER TABLE orders.order_items 
ADD CONSTRAINT fk_order_items_order_id 
FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE;

-- Thêm constraint kiểm tra logic
ALTER TABLE orders.orders 
ADD CONSTRAINT check_subtotal CHECK (subtotal >= 0);

ALTER TABLE orders.orders 
ADD CONSTRAINT check_discount_amount CHECK (discount_amount >= 0);

ALTER TABLE orders.orders 
ADD CONSTRAINT check_total_amount CHECK (total_amount >= 0);

-- Tạo function để tự động generate order code
CREATE OR REPLACE FUNCTION generate_order_code() RETURNS TEXT AS $$
DECLARE
    new_code TEXT;
    counter INTEGER := 1;
BEGIN
    LOOP
        new_code := 'ORD' || TO_CHAR(NOW(), 'YYYYMMDD') || LPAD(counter::TEXT, 4, '0');
        
        -- Kiểm tra xem code đã tồn tại chưa
        IF NOT EXISTS (SELECT 1 FROM orders.orders WHERE code = new_code) THEN
            RETURN new_code;
        END IF;
        
        counter := counter + 1;
        
        -- Tránh vòng lặp vô hạn
        IF counter > 9999 THEN
            new_code := 'ORD' || TO_CHAR(NOW(), 'YYYYMMDDHH24MISS') || LPAD((EXTRACT(EPOCH FROM NOW())::BIGINT % 1000)::TEXT, 3, '0');
            RETURN new_code;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql; 