-- 更新管理员密码为 MD5 密文
UPDATE admin_users SET password = '0192023a7bbd73250516f069df18b500' WHERE username = 'admin';
UPDATE admin_users SET password = '0795151defba7a4b5dfa89170de46277' WHERE username = 'manager';
UPDATE admin_users SET password = 'dbb8c54ee649f8af049357a5f99cede6' WHERE username = 'cashier01';
UPDATE admin_users SET password = 'e7bca0b30b7fdb9f0ecbb7832c5f5348' WHERE username = 'warehouse01';
UPDATE admin_users SET password = '4751368fbef4cc9420716a698d0c393a' WHERE username = 'product01';
UPDATE admin_users SET password = '3df2c034f564ae53106c928b7278d1ca' WHERE username = 'service01';

-- 更新顾客密码为 MD5 密文
UPDATE users SET password = '6ad14ba9986e3615423dfca256d04e3f' WHERE username = '13800138001';
UPDATE users SET password = '6ad14ba9986e3615423dfca256d04e3f' WHERE username = '13800138002';
UPDATE users SET password = '6ad14ba9986e3615423dfca256d04e3f' WHERE username = '13800138003';

-- 更新配送员密码为 MD5 密文
UPDATE delivery_persons SET password = '1ed4a8186a0606351d36115f28cc3793' WHERE phone = '13900000001';
UPDATE delivery_persons SET password = '1ed4a8186a0606351d36115f28cc3793' WHERE phone = '13900000002';
UPDATE delivery_persons SET password = '1ed4a8186a0606351d36115f28cc3793' WHERE phone = '13900000003';

COMMIT;
