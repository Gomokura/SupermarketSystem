set linesize 200
set pagesize 50
column product_id format 9999
column product_name format a30
column cover_image format a80

SELECT product_id, product_name, cover_image 
FROM products 
WHERE product_id IN (1000, 1001, 1002, 1003)
ORDER BY product_id;

exit
