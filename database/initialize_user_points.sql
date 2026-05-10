-- 根据订单消费额初始化用户积分
-- 规则：消费¥1 = 积分1，按支付金额计算（已去重）
-- 执行步骤：
-- 1. 直接在SQLPlus或SQL Developer中复制本脚本内容执行
-- 2. 或使用：sqlplus -S system/Oracle123@xe @/path/to/initialize_user_points.sql

-- ========================================
-- 步骤1: 临时表 - 计算每个用户的总消费额
-- ========================================
CREATE GLOBAL TEMPORARY TABLE temp_user_points AS
SELECT 
  user_id,
  FLOOR(SUM(pay_amount)) as total_points
FROM orders
WHERE status IN ('PAID', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CLOSED')
GROUP BY user_id;

-- ========================================
-- 步骤2: 更新用户积分
-- ========================================
UPDATE users u
SET u.points = (
  SELECT COALESCE(tmp.total_points, 0)
  FROM temp_user_points tmp
  WHERE tmp.user_id = u.user_id
)
WHERE u.user_id IN (SELECT user_id FROM temp_user_points);

COMMIT;

-- ========================================
-- 步骤3: 验证更新结果
-- ========================================
SELECT 
  u.user_id,
  u.username as 账户,
  u.points as 当前积分,
  COUNT(DISTINCT o.order_id) as 订单数,
  SUM(o.pay_amount) as 消费总额
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id 
  AND o.status IN ('PAID', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CLOSED')
WHERE u.user_id < 2000
GROUP BY u.user_id, u.username, u.points
ORDER BY u.user_id;

-- ========================================
-- 步骤4: 清理临时表
-- ========================================
DROP TABLE temp_user_points;

-- 完成！
