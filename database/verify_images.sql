set pagesize 50
SELECT product_id, product_name, length(cover_image) as img_len, SUBSTR(cover_image, 1, 60) 
FROM products 
WHERE product_id IN (1000, 1001, 1002)
ORDER BY product_id;
exit
