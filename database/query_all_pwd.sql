SET LINESIZE 1000
SET PAGESIZE 100
COLUMN username FORMAT A20
COLUMN password FORMAT A60
COLUMN real_name FORMAT A20

PROMPT === ADMIN USERS ===
SELECT admin_id, username, password, real_name, role FROM admin_users;

PROMPT
PROMPT === DELIVERY_PERSONS ===
SELECT courier_id, real_name, phone, password, status FROM delivery_persons;

PROMPT
PROMPT === USERS ===
SELECT user_id, username, password, nickname, real_name, member_level FROM users;

EXIT;
