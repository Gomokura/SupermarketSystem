-- ============================================================
-- 默认管理员账号（便于本地/Apifox 联调）
-- 用户名: admin  密码: 123456（与 AuthService 中 MD5 存储一致）
-- MD5('123456') = e10adc3949ba59abbe56e057f20f883e
-- 若已存在同名用户则跳过
-- ============================================================
INSERT INTO ADMINS (username, password, real_name, role, status, create_time)
SELECT 'admin',
       'e10adc3949ba59abbe56e057f20f883e',
       '系统管理员',
       'super_admin',
       'active',
       SYSDATE
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ADMINS WHERE username = 'admin');
