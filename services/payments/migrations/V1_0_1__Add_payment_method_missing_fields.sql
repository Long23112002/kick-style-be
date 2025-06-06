-- Thêm các cột thiếu cho bảng payment_method
ALTER TABLE payments.payment_method 
ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE payments.payment_method 
ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE;

-- Cập nhật dữ liệu hiện tại
UPDATE payments.payment_method SET 
  is_active = TRUE
WHERE is_active IS NULL; 