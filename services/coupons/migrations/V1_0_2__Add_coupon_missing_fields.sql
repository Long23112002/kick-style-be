-- Thêm các cột thiếu cho bảng coupons
ALTER TABLE coupons.coupons 
ADD COLUMN IF NOT EXISTS name VARCHAR(255) NOT NULL DEFAULT 'Default Coupon Name';

ALTER TABLE coupons.coupons 
ADD COLUMN IF NOT EXISTS max_usage_count INTEGER;

ALTER TABLE coupons.coupons 
ADD COLUMN IF NOT EXISTS valid_from TIMESTAMP;

ALTER TABLE coupons.coupons 
ADD COLUMN IF NOT EXISTS valid_to TIMESTAMP;

-- Cập nhật dữ liệu hiện tại để đồng bộ
UPDATE coupons.coupons SET 
  valid_from = start_date,
  valid_to = end_date,
  max_usage_count = usage_limit
WHERE valid_from IS NULL OR valid_to IS NULL;

-- Cập nhật tên mặc định dựa trên description
UPDATE coupons.coupons SET 
  name = COALESCE(description, 'Coupon ' || code)
WHERE name = 'Default Coupon Name'; 