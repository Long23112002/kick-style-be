-- Tạo schema payments nếu chưa có
CREATE SCHEMA IF NOT EXISTS payments;

-- Tạo bảng payment_method
CREATE TABLE payments.payment_method
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN   DEFAULT FALSE
);

-- Thêm dữ liệu mặc định cho các phương thức thanh toán
INSERT INTO payments.payment_method (name, slug) VALUES
('Thanh toán khi nhận hàng (COD)', 'cod'),
('Chuyển khoản ngân hàng', 'bank-transfer'),
('Ví điện tử MoMo', 'momo'),
('Ví điện tử ZaloPay', 'zalopay'),
('Thẻ tín dụng/ghi nợ', 'credit-card'),
('Ví điện tử VNPay', 'vnpay'); 