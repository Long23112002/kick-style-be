-- Thêm cột coupon_id vào bảng orders
ALTER TABLE orders.orders 
ADD COLUMN coupon_id BIGINT,
ADD COLUMN coupon_code VARCHAR(50); -- Lưu lại mã coupon để tránh mất dữ liệu khi coupon bị xóa

-- Tạo index cho coupon_id và coupon_code
CREATE INDEX idx_orders_coupon_id ON orders.orders(coupon_id);
CREATE INDEX idx_orders_coupon_code ON orders.orders(coupon_code);

-- Thêm bảng order_coupons để lưu lịch sử sử dụng coupon chi tiết
CREATE TABLE orders.order_coupons
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT        NOT NULL,
    coupon_id       BIGINT        NOT NULL,
    coupon_code     VARCHAR(50)   NOT NULL,
    discount_type   VARCHAR(20)   NOT NULL,
    discount_value  NUMERIC(10,2) NOT NULL,
    discount_amount NUMERIC(10,2) NOT NULL, -- Số tiền thực tế được giảm
    created_at      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Tạo foreign key constraint
ALTER TABLE orders.order_coupons 
ADD CONSTRAINT fk_order_coupons_order_id 
FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE;

-- Tạo index
CREATE INDEX idx_order_coupons_order_id ON orders.order_coupons(order_id);
CREATE INDEX idx_order_coupons_coupon_id ON orders.order_coupons(coupon_id);
CREATE INDEX idx_order_coupons_coupon_code ON orders.order_coupons(coupon_code); 