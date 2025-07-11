-- Thêm cột is_enabled vào bảng product_variants
ALTER TABLE products.product_variants 
ADD COLUMN is_enabled BOOLEAN NOT NULL DEFAULT TRUE;