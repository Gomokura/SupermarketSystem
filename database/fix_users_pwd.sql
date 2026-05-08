-- 更新 USERS 表占位符密码为真实 BCrypt 哈希
-- 密码 "123456" 的 BCrypt hash (rounds=10)
-- BCrypt $2b$ 和 $2a$ 前缀可互操作

UPDATE USERS SET password = '$2b$10$CFve8AHyZlr9XDNYVL4HzODkpRwf7c73qYajwi3kCfQUZbMUkQ9Ge' WHERE user_id = 1001;
COMMIT;
