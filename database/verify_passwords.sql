SELECT 'ADMIN USERS' AS source, username, password FROM admin_users WHERE username IN ('admin','manager','cashier01','warehouse01','product01','service01');
SELECT 'CUSTOMER USERS' AS source, username, password FROM users WHERE username IN ('13800138001','13800138002','13800138003');
SELECT 'DELIVERY PERSONS' AS source, phone AS username, password FROM delivery_persons WHERE phone IN ('13900000001','13900000002','13900000003');
