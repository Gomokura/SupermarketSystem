@echo off
chcp 65001 >nul
cd /d %~dp0

echo ========================================
echo Supermarket System - Database Init
echo ========================================
echo.

sqlplus system/123456@localhost:1521/XE @database/01_tables.sql
sqlplus system/123456@localhost:1521/XE @database/02_orders_inventory.sql
sqlplus system/123456@localhost:1521/XE @database/03_sequences_indexes.sql
sqlplus system/123456@localhost:1521/XE @database/04_init_data.sql

echo.
echo ========================================
echo Database initialization completed!
echo ========================================
pause
