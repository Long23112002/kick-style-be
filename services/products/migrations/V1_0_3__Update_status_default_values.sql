-- Cập nhật giá trị mặc định cho status trong bảng products
UPDATE products.products 
SET status = 'ACTIVE' 
WHERE status IS NULL;

-- Cập nhật giá trị mặc định cho status trong bảng product_variants
UPDATE products.product_variants 
SET status = CASE 
    WHEN stock_quantity IS NULL OR stock_quantity <= 0 THEN 'OUT_OF_STOCK'
    ELSE 'ACTIVE'
END
WHERE status IS NULL;

-- Thiết lập constraint NOT NULL và default value cho status
ALTER TABLE products.products 
ALTER COLUMN status SET NOT NULL,
ALTER COLUMN status SET DEFAULT 'ACTIVE';

ALTER TABLE products.product_variants 
ALTER COLUMN status SET NOT NULL,
ALTER COLUMN status SET DEFAULT 'ACTIVE';

-- Thêm các ràng buộc để đảm bảo status chỉ có thể là ACTIVE, INACTIVE, hoặc OUT_OF_STOCK
ALTER TABLE products.products
ADD CONSTRAINT products_status_check 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK'));

ALTER TABLE products.product_variants
ADD CONSTRAINT product_variants_status_check 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK')); 