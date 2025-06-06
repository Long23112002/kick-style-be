-- Tạo bảng quan hệ giữa coupon và user (ai được phép dùng coupon)
CREATE TABLE coupons.coupon_users
(
    id         BIGSERIAL PRIMARY KEY,
    coupon_id  BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coupon_id, user_id)
);

-- Tạo bảng lưu lịch sử sử dụng coupon của từng user
CREATE TABLE coupons.user_coupon_usage
(
    id         BIGSERIAL PRIMARY KEY,
    coupon_id  BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    order_id   BIGINT    NOT NULL,
    used_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(coupon_id, user_id) -- Mỗi user chỉ dùng được 1 lần mỗi coupon
);

-- Tạo index để tối ưu query
CREATE INDEX idx_coupon_users_coupon_id ON coupons.coupon_users(coupon_id);
CREATE INDEX idx_coupon_users_user_id ON coupons.coupon_users(user_id);

CREATE INDEX idx_user_coupon_usage_coupon_id ON coupons.user_coupon_usage(coupon_id);
CREATE INDEX idx_user_coupon_usage_user_id ON coupons.user_coupon_usage(user_id);
CREATE INDEX idx_user_coupon_usage_order_id ON coupons.user_coupon_usage(order_id);

-- Thêm cột user_specific vào bảng coupons để đánh dấu coupon có giới hạn user không
ALTER TABLE coupons.coupons 
ADD COLUMN user_specific BOOLEAN DEFAULT FALSE; 