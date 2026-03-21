@echo off
chcp 65001 >nul
cd /d %~dp0

echo ========================================
echo Supermarket System - Build
echo ========================================
echo.

call mvn clean package

echo.
echo ========================================
echo Build completed!
echo WAR file: target/SupermarketSystem.war
echo.
echo Copy WAR to Tomcat webapps folder
echo Then start Tomcat
echo ========================================
pause
