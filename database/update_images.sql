-- ============================================================
-- Update Images for Banners and Products
-- File: update_images.sql
-- Purpose: Update banner and product images with reliable placeholder URLs
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF
SET DEFINE OFF

PROMPT Updating banner and product images...

-- ============================================================
-- 1. Update Banners with generated images
-- ============================================================
UPDATE banners SET image_url = 'https://picsum.photos/800/320?random=1' WHERE banner_id = 1;
UPDATE banners SET image_url = 'https://picsum.photos/800/320?random=2' WHERE banner_id = 2;
UPDATE banners SET image_url = 'https://picsum.photos/800/320?random=3' WHERE banner_id = 3;
UPDATE banners SET image_url = 'https://picsum.photos/800/320?random=4' WHERE banner_id = 4;

-- ============================================================
-- 2. Update Products with cover images
-- ============================================================
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1001' WHERE product_id = 1000;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1002' WHERE product_id = 1001;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1003' WHERE product_id = 1002;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1004' WHERE product_id = 1003;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1005' WHERE product_id = 1004;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1006' WHERE product_id = 1005;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1007' WHERE product_id = 1006;
UPDATE products SET cover_image = 'https://picsum.photos/300/300?random=1008' WHERE product_id = 1007;

COMMIT;

PROMPT Image update completed successfully!


