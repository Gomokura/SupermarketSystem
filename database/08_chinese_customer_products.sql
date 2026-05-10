-- ============================================================
-- Supermarket Management System - Chinese Customer Product Data
-- Purpose: Fill customer category pages so every visible category has products.
-- Safe to run multiple times.
-- ============================================================

SET FEEDBACK OFF
SET ECHO OFF

PROMPT ============================================================
PROMPT Chinese customer product data started...
PROMPT ============================================================

DECLARE
    v_count NUMBER;

    PROCEDURE ensure_product(
        p_name IN VARCHAR2,
        p_barcode IN VARCHAR2,
        p_category_id IN NUMBER,
        p_brand_id IN NUMBER,
        p_supplier_id IN NUMBER,
        p_unit IN VARCHAR2,
        p_cost IN NUMBER,
        p_original IN NUMBER,
        p_price IN NUMBER,
        p_stock IN NUMBER,
        p_sales IN NUMBER,
        p_recommend IN NUMBER,
        p_description IN VARCHAR2
    ) IS
    BEGIN
        SELECT COUNT(*) INTO v_count
          FROM products
         WHERE barcode = p_barcode
            OR product_name = p_name;

        IF v_count = 0 THEN
            INSERT INTO products (
                product_id, product_name, barcode, category_id, brand_id, supplier_id,
                description, unit, cost_price, original_price, price, stock,
                stock_warning, sales_count, avg_rating, is_recommend, has_sku,
                status, is_deleted, create_time, update_time
            ) VALUES (
                seq_products.NEXTVAL, p_name, p_barcode, p_category_id, p_brand_id, p_supplier_id,
                p_description, p_unit, p_cost, p_original, p_price, p_stock,
                20, p_sales, 4.8, p_recommend, 0,
                'active', 0, SYSTIMESTAMP, SYSTIMESTAMP
            );
        ELSE
            UPDATE products
               SET category_id = p_category_id,
                   brand_id = p_brand_id,
                   supplier_id = p_supplier_id,
                   unit = p_unit,
                   cost_price = p_cost,
                   original_price = p_original,
                   price = p_price,
                   stock = GREATEST(NVL(stock, 0), p_stock),
                   sales_count = GREATEST(NVL(sales_count, 0), p_sales),
                   avg_rating = NVL(avg_rating, 4.8),
                   is_recommend = p_recommend,
                   status = 'active',
                   is_deleted = 0,
                   update_time = SYSTIMESTAMP
             WHERE barcode = p_barcode
                OR product_name = p_name;
        END IF;
    END;
BEGIN
    ensure_product('百事可乐 330ml', '6901234510001', 6, 1, 1, '罐', 2.10, 4.00, 3.60, 360, 2680, 1, '冰镇更爽的经典碳酸饮料');
    ensure_product('雪碧柠檬味汽水 330ml', '6901234510002', 6, 1, 1, '罐', 2.00, 4.00, 3.50, 320, 2360, 1, '清爽柠檬味汽水');
    ensure_product('怡宝饮用纯净水 555ml', '6901234510003', 7, 2, 2, '瓶', 0.90, 2.50, 2.00, 780, 3880, 1, '日常饮用纯净水');
    ensure_product('景田百岁山矿泉水 570ml', '6901234510004', 7, 2, 2, '瓶', 1.40, 3.50, 3.00, 520, 1960, 0, '天然矿泉水');

    ensure_product('良品铺子每日坚果 750g', '6901234510005', 9, 3, 3, '盒', 58.00, 99.00, 79.90, 120, 1680, 1, '混合坚果礼盒装');
    ensure_product('洽洽原香瓜子 308g', '6901234510006', 9, 3, 3, '袋', 8.50, 16.90, 12.90, 260, 2240, 0, '经典原香瓜子');
    ensure_product('德芙丝滑牛奶巧克力 252g', '6901234510007', 10, 8, 3, '盒', 24.00, 45.00, 36.90, 180, 1420, 1, '丝滑牛奶巧克力分享装');
    ensure_product('徐福记酥心糖 500g', '6901234510008', 10, 8, 3, '袋', 18.00, 35.00, 26.80, 210, 1320, 0, '节日休闲糖果');
    ensure_product('达利园法式小面包 360g', '6901234510009', 2, 8, 3, '袋', 12.00, 26.00, 19.90, 240, 1880, 1, '早餐代餐小面包');

    ensure_product('舒肤佳柠檬清新沐浴露 720ml', '6901234510010', 11, 6, 1, '瓶', 24.00, 49.90, 39.90, 160, 980, 1, '清爽洁净沐浴露');
    ensure_product('云南白药留兰香牙膏 180g', '6901234510011', 11, 6, 1, '支', 15.00, 32.00, 25.90, 220, 1160, 0, '清新口气护理牙膏');
    ensure_product('蓝月亮深层洁净洗衣液 3kg', '6901234510012', 12, 5, 1, '瓶', 32.00, 69.90, 55.90, 140, 1520, 1, '家庭衣物清洁洗衣液');
    ensure_product('威猛先生厨房重油污净 500g', '6901234510013', 12, 5, 1, '瓶', 14.00, 29.90, 22.90, 190, 870, 0, '厨房油污清洁剂');

    ensure_product('海天上等蚝油 700g', '6901234510014', 5, 5, 1, '瓶', 7.00, 18.90, 13.90, 260, 1750, 0, '家常炒菜调味蚝油');
    ensure_product('李锦记蒸鱼豉油 410ml', '6901234510015', 5, 5, 1, '瓶', 8.50, 19.90, 15.80, 210, 960, 0, '蒸鱼凉拌调味酱油');

    ensure_product('泸州老窖头曲 52度 500ml', '6901234510016', 13, 5, 3, '瓶', 68.00, 129.00, 99.00, 80, 360, 0, '浓香型白酒');
    ensure_product('汾酒玻汾 53度 475ml', '6901234510017', 13, 5, 3, '瓶', 46.00, 89.00, 68.00, 95, 420, 0, '清香型白酒');
    ensure_product('青岛啤酒经典 500ml', '6901234510018', 14, 1, 3, '罐', 3.80, 8.00, 6.50, 360, 2180, 1, '经典清爽啤酒');
    ensure_product('雪花勇闯天涯 500ml', '6901234510019', 14, 1, 3, '罐', 3.60, 7.50, 5.90, 420, 2460, 1, '聚会常备啤酒');

    COMMIT;
END;
/

COMMIT;

PROMPT ============================================================
PROMPT Chinese customer product data completed.
PROMPT ============================================================
